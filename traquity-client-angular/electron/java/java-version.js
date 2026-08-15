const path = require('node:path');
const {StringDecoder} = require('node:string_decoder');
const {jvmEnvironment} = require('./jvm-environment.js');

/**
 * Runs `java -version` against one binary path and reports the verdict. The banner a JVM prints goes to **stderr**,
 * not stdout (`--version` is the stdout variant, but predates JDK 9 and would error out on a too-old runtime) - stderr
 * is therefore preferred whenever it carries anything, stdout only as a fallback. The run is asynchronous
 * (`child_process.spawn`, never `spawnSync`, which would block the whole main process) and bounded by a timeout, so a
 * pathological binary can never hang the caller.
 *
 * The binary is whatever a path names, so nothing here assumes it is a JVM, or benign: only an absolute path named
 * `java`/`java.exe` is run at all, and it is spawned without a shell, with an argument vector, with no stdin, with the
 * environment `jvm-environment.js` allows a JVM, and both of its output streams are read under a byte budget. Every
 * failure of the run - a refused path, a throwing spawn, an unreadable stream, an overrun budget, a timeout - resolves
 * to an `error` verdict; the returned promise never rejects, and no event this module subscribes to can throw into the
 * process it runs in.
 */

/** @typedef {{status: 'ok', javaPath: string, versionOutput: string} | {status: 'error', message: string}} JavaVerification */

/** @type {number} */
const DEFAULT_TIMEOUT_MILLIS = 10_000;

/**
 * The budget for everything the probed binary writes, stdout and stderr together. A JVM banner is a few hundred bytes,
 * so this bounds nothing real; what it does bound is the case, where output is produced faster than any
 * timeout can end it and accumulating it costs the reading process its memory.
 *
 * @type {number}
 */
const MAX_OUTPUT_BYTES = 64 * 1024;

/**
 * The names a java binary goes by, lowercased for the comparison. Refusing everything else keeps this from being a way
 * to run an arbitrary program: what reaches the spawn is then a path the user named `java`, not merely a path. It does not
 * protect against an attacker disguising a malicious file under this name, it merely protects from accidentally selecting
 * and starting a wrong executable.
 *
 * @type {string[]}
 */
const JAVA_BINARY_NAMES = ['java', 'java.exe'];

/**
 * @typedef {Object} JavaVersionStream
 * @property {((event: 'data', listener: (chunk: Buffer) => void) => void)
 *   & ((event: 'error', listener: (error: Error) => void) => void)} on
 */

/**
 * Subset of `ChildProcessWithoutNullStreams` this module actually touches - declared minimally, per the directory's
 * rule. `on` is typed as an intersection of its two call shapes rather than one shared signature, because the two
 * listeners genuinely take different arguments.
 *
 * @typedef {Object} JavaVersionChildProcess
 * @property {JavaVersionStream} stdout
 * @property {JavaVersionStream} stderr
 * @property {((event: 'exit', listener: (code: number | null) => void) => void)
 *   & ((event: 'error', listener: (error: Error) => void) => void)} on
 * @property {() => void} kill
 */

/**
 * The options every probe is spawned with. `stdio` denies the child a stdin - one that reads it would otherwise wait
 * for input that never comes, until the timeout - while keeping its two output streams as pipes, so that what it
 * writes is read here rather than inherited into this process's own streams.
 *
 * @typedef {Object} JavaVersionSpawnOptions
 * @property {NodeJS.ProcessEnv} env
 * @property {['ignore', 'pipe', 'pipe']} stdio
 */

/**
 * @typedef {Object} RunJavaVersionOptions
 * @property {(command: string, args: string[], options: JavaVersionSpawnOptions) => JavaVersionChildProcess} spawn
 * @property {number} [timeoutMillis]
 */

/**
 * @param {string} text
 * @returns {string}
 */
function withoutTrailingNewlines(text) {
  return text.replace(/[\r\n]+$/, '');
}

/**
 * @param {unknown} error
 * @returns {string}
 */
function messageOf(error) {
  return error instanceof Error ? error.message : String(error);
}

/**
 * @param {string} binaryPath
 * @returns {boolean}
 */
function isJavaBinary(binaryPath) {
  return JAVA_BINARY_NAMES.includes(path.basename(binaryPath).toLowerCase());
}

/**
 * @param {string} binaryPath
 * @param {RunJavaVersionOptions} options
 * @returns {Promise<JavaVerification>}
 */
function runJavaVersion(binaryPath, options) {
  const {spawn} = options;
  const timeoutMillis = options.timeoutMillis ?? DEFAULT_TIMEOUT_MILLIS;

  return new Promise(resolve => {
    let settled = false;
    let stdout = '';
    let stderr = '';
    let capturedBytes = 0;

    // declared before anything can settle, and assigned only once there is a child to time out: a settle that happens
    // before the spawn then clears an unarmed timer instead of reading a binding that does not exist yet
    /** @type {NodeJS.Timeout | undefined} */
    let timer;

    /**
     * @param {JavaVerification} verification
     * @returns {void}
     */
    function settle(verification) {
      if (settled) {
        return;
      }
      settled = true;
      clearTimeout(timer);
      resolve(verification);
    }

    if (!path.isAbsolute(binaryPath)) {
      settle({status: 'error', message: `${binaryPath} is not an absolute path`});
      return;
    }

    if (!isJavaBinary(binaryPath)) {
      settle({status: 'error', message: `${binaryPath} is not a java binary`});
      return;
    }

    let child;
    try {
      child = spawn(binaryPath, ['-version'], {env: jvmEnvironment(process.env), stdio: ['ignore', 'pipe', 'pipe']});
    } catch (error) {
      settle({status: 'error', message: `Failed to start ${binaryPath}: ${messageOf(error)}`});
      return;
    }
    const spawnedProcess = child;

    /**
     * Reads one stream into `append` under the shared byte budget: output past it ends the run with an `error` verdict
     * instead of being accumulated, and a chunk arriving after the verdict is dropped, so a child that survives its
     * `kill()` cannot go on growing a string nobody will read. Decoding is stateful across chunks, so a multi-byte
     * character split over a chunk boundary survives it.
     *
     * @param {JavaVersionStream} stream
     * @param {(text: string) => void} append
     * @returns {void}
     */
    function capture(stream, append) {
      const decoder = new StringDecoder('utf8');

      stream.on('data', chunk => {
        if (settled) {
          return;
        }
        capturedBytes += chunk.length;
        if (capturedBytes > MAX_OUTPUT_BYTES) {
          spawnedProcess.kill();
          settle({status: 'error', message: `${binaryPath} -version wrote more than ${MAX_OUTPUT_BYTES} bytes`});
          return;
        }
        append(decoder.write(chunk));
      });

      stream.on('error', error => {
        settle({status: 'error', message: `Failed to read the output of ${binaryPath}: ${messageOf(error)}`});
      });
    }

    timer = setTimeout(() => {
      spawnedProcess.kill();
      settle({status: 'error', message: `${binaryPath} -version did not respond within ${timeoutMillis}ms`});
    }, timeoutMillis);

    capture(spawnedProcess.stdout, text => {
      stdout += text;
    });
    capture(spawnedProcess.stderr, text => {
      stderr += text;
    });

    spawnedProcess.on('error', error => {
      settle({status: 'error', message: `Failed to start ${binaryPath}: ${error.message}`});
    });

    spawnedProcess.on('exit', code => {
      if (code === 0) {
        const banner = withoutTrailingNewlines(stderr.length > 0 ? stderr : stdout);
        settle({status: 'ok', javaPath: binaryPath, versionOutput: banner});
        return;
      }
      settle({status: 'error', message: `${binaryPath} -version exited with code ${code}`});
    });
  });
}

module.exports = {runJavaVersion, DEFAULT_TIMEOUT_MILLIS, MAX_OUTPUT_BYTES};
