const path = require('node:path');
const {afterEach, beforeEach, describe, expect, it, jest} = require('@jest/globals');
const {runJavaVersion, MAX_OUTPUT_BYTES} = require('./java-version.js');

/** @import {JavaVersionChildProcess, JavaVersionStream} from './java-version.js' */

/**
 * A manually driven stand-in for one of the probed process's output streams.
 *
 * @returns {{stream: JavaVersionStream, emitData: (chunk: string | Buffer) => void, emitError: (error: Error) => void}}
 */
function createFakeStream() {
  /** @type {((chunk: Buffer) => void)[]} */
  const dataListeners = [];
  /** @type {((error: Error) => void)[]} */
  const errorListeners = [];

  /** @type {JavaVersionStream} */
  const stream = {
    on: /** @type {JavaVersionStream['on']} */ ((event, listener) => {
      if (event === 'data') {
        dataListeners.push(/** @type {(chunk: Buffer) => void} */ (listener));
      } else {
        errorListeners.push(/** @type {(error: Error) => void} */ (listener));
      }
    })
  };

  return {
    stream,
    emitData: (chunk) => {
      const bytes = Buffer.isBuffer(chunk) ? chunk : Buffer.from(chunk, 'utf8');
      dataListeners.forEach(listener => listener(bytes));
    },
    emitError: (error) => errorListeners.forEach(listener => listener(error))
  };
}

/**
 * A minimal, manually driven stand-in for a spawned `java -version` child process: tests drive it by calling the
 * returned `emit*` functions instead of by waiting on a real process, which is what makes the timeout case
 * deterministic under fake timers.
 *
 * @returns {{child: JavaVersionChildProcess, emitStdout: (chunk: string | Buffer) => void,
 *   emitStderr: (chunk: string | Buffer) => void, emitStdoutError: (error: Error) => void,
 *   emitStderrError: (error: Error) => void, emitExit: (code: number | null) => void,
 *   emitError: (error: Error) => void, kill: jest.Mock<() => void>}}
 */
function createFakeChild() {
  const stdout = createFakeStream();
  const stderr = createFakeStream();

  /** @type {((code: number | null) => void)[]} */
  const exitListeners = [];
  /** @type {((error: Error) => void)[]} */
  const errorListeners = [];

  const kill = jest.fn(/** @type {() => void} */ (() => undefined));

  /** @type {JavaVersionChildProcess} */
  const child = {
    stdout: stdout.stream,
    stderr: stderr.stream,
    on: /** @type {JavaVersionChildProcess['on']} */ ((event, listener) => {
      if (event === 'exit') {
        exitListeners.push(/** @type {(code: number | null) => void} */ (listener));
      } else {
        errorListeners.push(/** @type {(error: Error) => void} */ (listener));
      }
    }),
    kill
  };

  return {
    child,
    emitStdout: stdout.emitData,
    emitStderr: stderr.emitData,
    emitStdoutError: stdout.emitError,
    emitStderrError: stderr.emitError,
    emitExit: (code) => exitListeners.forEach(listener => listener(code)),
    emitError: (error) => errorListeners.forEach(listener => listener(error)),
    kill
  };
}

describe('runJavaVersion', () => {
  // absolute on whichever platform the suite runs on, since the module refuses everything else
  const binaryPath = path.resolve(path.sep, 'jdk', 'bin', 'java.exe');

  /** @type {ReturnType<typeof createFakeChild>} */
  let fakeChild;

  const spawn = jest.fn(/** @type {(command: string, args: string[],
   options: import('./java-version.js').JavaVersionSpawnOptions) => JavaVersionChildProcess} */ (() => fakeChild.child));

  beforeEach(() => {
    jest.clearAllMocks();
    fakeChild = createFakeChild();
    spawn.mockImplementation(() => fakeChild.child);
  });

  afterEach(() => {
    delete process.env['_JAVA_OPTIONS'];
    delete process.env['JAVA_TOOL_OPTIONS'];
    delete process.env['JDK_JAVA_OPTIONS'];
    delete process.env['TQ_DB_FILE_PASSWORD'];
  });

  it('spawns java -version with no shell, no stdin and both output streams piped', async () => {
    const resultPromise = runJavaVersion(binaryPath, {spawn});
    fakeChild.emitStderr('openjdk version "25" 2025-09-16\n');
    fakeChild.emitExit(0);

    /** @type {NodeJS.ProcessEnv} */
    const expectedEnvironment = {...process.env};
    for (const key of ['TQ_DB_FILE_PASSWORD', 'JAVA_TOOL_OPTIONS', 'JDK_JAVA_OPTIONS', '_JAVA_OPTIONS']) {
      delete expectedEnvironment[key];
    }
    await expect(resultPromise).resolves.toEqual({
      status: 'ok',
      javaPath: binaryPath,
      versionOutput: 'openjdk version "25" 2025-09-16'
    });
    expect(spawn).toHaveBeenCalledTimes(1);
    expect(spawn).toHaveBeenCalledWith(binaryPath, ['-version'], {
      env: expectedEnvironment,
      stdio: ['ignore', 'pipe', 'pipe']
    });
  });

  it('reports the trimmed stderr banner as ok on exit code 0', async () => {
    const resultPromise = runJavaVersion(binaryPath, {spawn});
    fakeChild.emitStderr('openjdk version "25" 2025-09-16\n');
    fakeChild.emitExit(0);

    await expect(resultPromise).resolves.toEqual({
      status: 'ok',
      javaPath: binaryPath,
      versionOutput: 'openjdk version "25" 2025-09-16'
    });
  });

  it('falls back to stdout when stderr carries nothing', async () => {
    const resultPromise = runJavaVersion(binaryPath, {spawn});
    fakeChild.emitStdout('openjdk 25\n');
    fakeChild.emitExit(0);

    await expect(resultPromise).resolves.toEqual({
      status: 'ok',
      javaPath: binaryPath,
      versionOutput: 'openjdk 25'
    });
  });

  it('prefers stderr over stdout when both carry output', async () => {
    const resultPromise = runJavaVersion(binaryPath, {spawn});
    fakeChild.emitStdout('from stdout\n');
    fakeChild.emitStderr('from stderr\n');
    fakeChild.emitExit(0);

    await expect(resultPromise).resolves.toEqual({
      status: 'ok',
      javaPath: binaryPath,
      versionOutput: 'from stderr'
    });
  });

  it('reassembles a character split across two chunks', async () => {
    const bannerBytes = Buffer.from('openjdk version "25" (Bü)\n', 'utf8');
    const insideTheUmlaut = bannerBytes.indexOf(0xc3) + 1;

    const resultPromise = runJavaVersion(binaryPath, {spawn});
    fakeChild.emitStderr(bannerBytes.subarray(0, insideTheUmlaut));
    fakeChild.emitStderr(bannerBytes.subarray(insideTheUmlaut));
    fakeChild.emitExit(0);

    await expect(resultPromise).resolves.toEqual({
      status: 'ok',
      javaPath: binaryPath,
      versionOutput: 'openjdk version "25" (Bü)'
    });
  });

  it('reports a non-zero exit as an error naming the code and the path', async () => {
    const resultPromise = runJavaVersion(binaryPath, {spawn});
    fakeChild.emitExit(1);

    await expect(resultPromise).resolves.toEqual({
      status: 'error',
      message: `${binaryPath} -version exited with code 1`
    });
  });

  it('reports a spawn error (e.g. ENOENT) as an error naming the path', async () => {
    const resultPromise = runJavaVersion(binaryPath, {spawn});
    fakeChild.emitError(new Error('spawn ENOENT'));

    await expect(resultPromise).resolves.toEqual({
      status: 'error',
      message: `Failed to start ${binaryPath}: spawn ENOENT`
    });
  });

  it('reports a throwing spawn as an error instead of rejecting', async () => {
    spawn.mockImplementation(() => {
      throw new Error('spawn EINVAL');
    });

    await expect(runJavaVersion(binaryPath, {spawn})).resolves.toEqual({
      status: 'error',
      message: `Failed to start ${binaryPath}: spawn EINVAL`
    });
  });

  it('reports an unreadable output stream as an error naming the path', async () => {
    const resultPromise = runJavaVersion(binaryPath, {spawn});
    fakeChild.emitStderrError(new Error('read ECONNRESET'));

    await expect(resultPromise).resolves.toEqual({
      status: 'error',
      message: `Failed to read the output of ${binaryPath}: read ECONNRESET`
    });
  });

  it('spawns nothing for a path the OS would have to resolve', async () => {
    await expect(runJavaVersion('java', {spawn})).resolves.toEqual({
      status: 'error',
      message: 'java is not an absolute path'
    });
    expect(spawn).not.toHaveBeenCalled();
  });

  it('spawns nothing for an absolute path to something other than a java binary', async () => {
    const otherBinary = path.resolve(path.sep, 'windows', 'system32', 'calc.exe');

    await expect(runJavaVersion(otherBinary, {spawn})).resolves.toEqual({
      status: 'error',
      message: `${otherBinary} is not a java binary`
    });
    expect(spawn).not.toHaveBeenCalled();
  });

  it('runs a java binary whose name is spelled in a different case', async () => {
    const shoutingPath = path.resolve(path.sep, 'jdk', 'bin', 'JAVA.EXE');

    const resultPromise = runJavaVersion(shoutingPath, {spawn});
    fakeChild.emitExit(0);

    await expect(resultPromise).resolves.toEqual({status: 'ok', javaPath: shoutingPath, versionOutput: ''});
    expect(spawn).toHaveBeenCalledTimes(1);
    expect(spawn).toHaveBeenCalledWith(shoutingPath, ['-version'], expect.objectContaining({stdio: ['ignore', 'pipe', 'pipe']}));
  });

  it('strips an inherited database password from the probed environment', async () => {
    process.env['TQ_DB_FILE_PASSWORD'] = 'from-the-shell';

    const resultPromise = runJavaVersion(binaryPath, {spawn});
    fakeChild.emitExit(0);
    await resultPromise;

    const environment = spawn.mock.calls[0]?.[2]?.env ?? {};
    expect(Object.keys(environment)).not.toContain('TQ_DB_FILE_PASSWORD');
    expect(JSON.stringify(environment)).not.toContain('from-the-shell');
  });

  it('strips the variables a JVM would take extra command-line arguments from', async () => {
    process.env['_JAVA_OPTIONS'] = '-javaagent:/tmp/underscore.jar';
    process.env['JAVA_TOOL_OPTIONS'] = '-javaagent:/tmp/tool.jar';
    process.env['JDK_JAVA_OPTIONS'] = '-javaagent:/tmp/jdk.jar';

    const resultPromise = runJavaVersion(binaryPath, {spawn});
    fakeChild.emitExit(0);
    await resultPromise;

    const environment = spawn.mock.calls[0]?.[2]?.env ?? {};
    expect(Object.keys(environment)).not.toContain('_JAVA_OPTIONS');
    expect(Object.keys(environment)).not.toContain('JAVA_TOOL_OPTIONS');
    expect(Object.keys(environment)).not.toContain('JDK_JAVA_OPTIONS');
    expect(JSON.stringify(environment)).not.toContain('javaagent');
  });

  describe('with a child that writes more than the output budget', () => {
    /** @type {Buffer} */
    let overflowingChunk;

    beforeEach(() => {
      overflowingChunk = Buffer.alloc(MAX_OUTPUT_BYTES + 1, 0x78);
    });

    it('kills the child and reports the overrun instead of accumulating the output', async () => {
      const resultPromise = runJavaVersion(binaryPath, {spawn});
      fakeChild.emitStderr(overflowingChunk);

      await expect(resultPromise).resolves.toEqual({
        status: 'error',
        message: `${binaryPath} -version wrote more than ${MAX_OUTPUT_BYTES} bytes`
      });
      expect(fakeChild.kill).toHaveBeenCalledTimes(1);
      expect(fakeChild.kill).toHaveBeenCalledWith();
    });

    it('budgets both output streams together', async () => {
      const resultPromise = runJavaVersion(binaryPath, {spawn});
      fakeChild.emitStdout(overflowingChunk.subarray(1));
      fakeChild.emitStderr(overflowingChunk.subarray(0, 1));

      await expect(resultPromise).resolves.toEqual({
        status: 'error',
        message: `${binaryPath} -version wrote more than ${MAX_OUTPUT_BYTES} bytes`
      });
    });

    it('keeps a run within the budget', async () => {
      const resultPromise = runJavaVersion(binaryPath, {spawn});
      fakeChild.emitStderr(overflowingChunk.subarray(1));
      fakeChild.emitExit(0);

      await expect(resultPromise).resolves.toEqual({
        status: 'ok',
        javaPath: binaryPath,
        versionOutput: 'x'.repeat(MAX_OUTPUT_BYTES)
      });
      expect(fakeChild.kill).not.toHaveBeenCalled();
    });
  });

  describe('with a child that never exits nor errors', () => {
    beforeEach(() => {
      jest.useFakeTimers();
    });

    afterEach(() => {
      jest.useRealTimers();
    });

    it('kills the child and reports a timeout after the configured duration', async () => {
      const resultPromise = runJavaVersion(binaryPath, {spawn, timeoutMillis: 10_000});

      await jest.advanceTimersByTimeAsync(10_000);

      await expect(resultPromise).resolves.toEqual({
        status: 'error',
        message: `${binaryPath} -version did not respond within 10000ms`
      });
      expect(fakeChild.kill).toHaveBeenCalledTimes(1);
      expect(fakeChild.kill).toHaveBeenCalledWith();
    });

    it('does not report a timeout before the configured duration has elapsed', async () => {
      const resultPromise = runJavaVersion(binaryPath, {spawn, timeoutMillis: 10_000});
      let settled = false;
      resultPromise.then(() => {
        settled = true;
      });

      await jest.advanceTimersByTimeAsync(9_999);

      expect(settled).toBe(false);
      expect(fakeChild.kill).not.toHaveBeenCalled();
    });

    it('drops output arriving after the verdict, however much of it there is', async () => {
      const resultPromise = runJavaVersion(binaryPath, {spawn, timeoutMillis: 10_000});
      await jest.advanceTimersByTimeAsync(10_000);
      await resultPromise;

      fakeChild.emitStderr(Buffer.alloc(MAX_OUTPUT_BYTES + 1, 0x78));

      // a second kill is what the budget would have triggered had those bytes still been read and counted
      expect(fakeChild.kill).toHaveBeenCalledTimes(1);
      expect(fakeChild.kill).toHaveBeenCalledWith();
    });
  });
});
