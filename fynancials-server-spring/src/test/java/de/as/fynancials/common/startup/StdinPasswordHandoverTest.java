package de.as.fynancials.common.startup;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.commons.logging.Log;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class StdinPasswordHandoverTest {

  private Log log;
  private CountDownLatch blockingLatch;

  @BeforeEach
  void beforeEach() {
    log = mock(Log.class);
    blockingLatch = new CountDownLatch(1);
  }

  @AfterEach
  void afterEach() {
    // releases a reader thread left blocked by the timeout test, so it cannot outlive the test run
    blockingLatch.countDown();
  }

  @Test
  void returnsTheStreamsEntireContent() {
    StdinPasswordHandover handover = handoverOf(new ByteArrayInputStream("hunter2".getBytes(UTF_8)));

    Optional<String> result = handover.read(log);

    assertThat(result).hasValue("hunter2");
    verifyNoInteractions(log);
  }

  @Test
  void decodesAMultiByteCharacterSplitAcrossTwoReads() throws IOException {
    byte[] bytes = "äöüßé".getBytes(UTF_8);
    AtomicInteger position = new AtomicInteger();
    InputStream input = mock(InputStream.class);
    when(input.read(any(byte[].class))).thenAnswer(invocation -> {
      int index = position.getAndIncrement();
      if (index >= bytes.length) {
        return -1;
      }
      invocation.<byte[]>getArgument(0)[0] = bytes[index];
      return 1;
    });

    Optional<String> result = handoverOf(input).read(log);

    assertThat(result).hasValue("äöüßé");
  }

  @Test
  void returnsAnEmptyPasswordForAnEmptyStream() {
    StdinPasswordHandover handover = handoverOf(new ByteArrayInputStream(new byte[0]));

    Optional<String> result = handover.read(log);

    assertThat(result).hasValue("");
  }

  @Test
  void keepsLeadingAndTrailingWhitespace() {
    StdinPasswordHandover handover = handoverOf(new ByteArrayInputStream(" secret \r".getBytes(UTF_8)));

    Optional<String> result = handover.read(log);

    assertThat(result).hasValue(" secret \r");
  }

  @Test
  void givesUpWhenTheStreamNeverReachesEof() throws IOException {
    InputStream input = mock(InputStream.class);
    when(input.read(any(byte[].class))).thenAnswer(invocation -> {
      blockingLatch.await();
      return -1;
    });

    Optional<String> result = new StdinPasswordHandover(input, Duration.ofMillis(100), 4096).read(log);

    assertThat(result).isEmpty();
  }

  @Test
  void givesUpWhenMoreThanTheLimitArrives() {
    StdinPasswordHandover handover =
        new StdinPasswordHandover(new ByteArrayInputStream("pass-".getBytes(UTF_8)),
            Duration.ofSeconds(5), 4);

    Optional<String> result = handover.read(log);

    assertThat(result).isEmpty();
  }

  @Test
  void acceptsAPasswordOfExactlyTheLimit() {
    StdinPasswordHandover handover =
        new StdinPasswordHandover(new ByteArrayInputStream("pass".getBytes(UTF_8)),
            Duration.ofSeconds(5), 4);

    Optional<String> result = handover.read(log);

    assertThat(result).hasValue("pass");
  }

  @Test
  void doesNotReadTheStreamASecondTime() {
    InputStream input = spy(new ByteArrayInputStream("hunter2".getBytes(UTF_8)));
    StdinPasswordHandover handover = handoverOf(input);
    handover.read(log);
    clearInvocations(input);

    Optional<String> result = handover.read(log);

    assertThat(result).hasValue("hunter2");
    verifyNoInteractions(input);
  }

  private static StdinPasswordHandover handoverOf(InputStream input) {
    return new StdinPasswordHandover(input, Duration.ofSeconds(5), 4096);
  }
}
