package de.as.fynancials.common.startup;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.logging.Log;

/**
 * Reads the database password handed over on the given {@link InputStream} to EOF, exactly once. The
 * bytes are treated as an opaque payload from first to last: read, decode, return - no trimming, no delimiter, no
 * format. A second read after the stream has already reached EOF would see it as an empty password, which is a
 * silent lockout, so the outcome of the first {@link #read} is memoized and every later invocation repeats it.
 */
@RequiredArgsConstructor
class StdinPasswordHandover {

  private final InputStream input;
  private final Duration timeout;
  private final int limitInBytes;

  private boolean attempted;
  private Optional<String> password = Optional.empty();

  /**
   * The stream's entire content, or empty when no handover completed (the read timed out, failed, or exceeded the
   * limit).
   */
  synchronized Optional<String> read(Log log) {
    if (!attempted) {
      attempted = true;
      password = readOnce(log);
    }
    return password;
  }

  private Optional<String> readOnce(Log log) {
    // deliberately not a try-with-resources, although ExecutorService is AutoCloseable: close() shuts the pool down
    // and then waits for it to terminate, which on the timeout path never happens - the reader thread stays blocked in
    // read() and is meant to be abandoned. The fallback below would then log its warning and hang forever on the way
    // out of the block, inside an EnvironmentPostProcessor, before the application context exists.
    ExecutorService executor = Executors.newSingleThreadExecutor(StdinPasswordHandover::daemonThread);
    try {
      Future<byte[]> bytes = executor.submit(this::readToEndOfFile);
      // decoded in one go, over the complete byte array: a multi-byte character can be split across two reads
      return Optional.of(new String(bytes.get(timeout.toMillis(), MILLISECONDS), UTF_8));
    } catch (TimeoutException e) {
      log.warn("No database password arrived on stdin within " + timeout + ", falling back to the environment");
      return Optional.empty();
    } catch (ExecutionException e) {
      log.warn("Failed to read the database password from stdin, falling back to the environment", e.getCause());
      return Optional.empty();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      return Optional.empty();
    } finally {
      // the reader thread is a daemon and is simply abandoned: a blocking read() on a pipe is not interruptible, so
      // shutdownNow() cannot cancel it, and a non-daemon one would keep the JVM from ever exiting
      executor.shutdownNow();
    }
  }

  private byte[] readToEndOfFile() throws IOException {
    ByteArrayOutputStream collected = new ByteArrayOutputStream();
    byte[] buffer = new byte[256];
    int count;
    // only -1 completes the handover: a short read is normal on a pipe and says nothing about the sender being done
    while ((count = input.read(buffer)) != -1) {
      if (collected.size() + count > limitInBytes) {
        throw new IOException("More than " + limitInBytes + " bytes arrived on stdin");
      }
      collected.write(buffer, 0, count);
    }
    return collected.toByteArray();
  }

  private static Thread daemonThread(Runnable runnable) {
    Thread thread = new Thread(runnable, "stdin-password-handover");
    thread.setDaemon(true);
    return thread;
  }
}
