const {beforeEach, describe, expect, it, jest} = require('@jest/globals');
const {createJavaRuntime} = require('./java-runtime.js');

/** @import {FynancialsConfig} from '../config/config-schema.js' */
/** @import {JavaVerification} from './java-version.js' */
/** @import {JavaRuntime} from './java-runtime.js' */

describe('javaRuntime', () => {
  const configuredPath = 'C:\\jdk\\bin\\java.exe';
  const normalizedConfiguredPath = 'C:\\jdk\\normalized\\java.exe';
  const pathCandidate = 'C:\\Program Files\\Java\\bin\\java.exe';

  /** @type {FynancialsConfig} */
  let config;

  /** @type {JavaRuntime} */
  let runtime;

  const findJavaOnPath = jest.fn(/** @type {() => string | null} */ (() => pathCandidate));
  const normalizeToJavaBinary = jest.fn(/** @type {(pathCandidate: string) => string} */ ((pathCandidate) => pathCandidate));
  const runJavaVersion = jest.fn(/** @type {(binaryPath: string) => Promise<JavaVerification>} */
    ((binaryPath) => Promise.resolve({status: 'ok', javaPath: binaryPath, versionOutput: 'openjdk 25'})));

  beforeEach(() => {
    jest.clearAllMocks();
    findJavaOnPath.mockReturnValue(pathCandidate);
    normalizeToJavaBinary.mockImplementation((pathCandidate) =>
      pathCandidate === configuredPath ? normalizedConfiguredPath : pathCandidate);
    runJavaVersion.mockImplementation((binaryPath) => Promise.resolve({status: 'ok', javaPath: binaryPath, versionOutput: 'openjdk 25'}));

    config = {env: {}, auth: {}, java: {path: configuredPath}};

    runtime = createJavaRuntime({config, findJavaOnPath, normalizeToJavaBinary, runJavaVersion});
  });

  describe('verifySetting', () => {
    it('normalizes and verifies a given setting', async () => {
      await expect(runtime.verifySetting(configuredPath)).resolves.toEqual({
        status: 'ok',
        javaPath: normalizedConfiguredPath,
        versionOutput: 'openjdk 25'
      });
      expect(normalizeToJavaBinary).toHaveBeenCalledTimes(1);
      expect(normalizeToJavaBinary).toHaveBeenCalledWith(configuredPath);
      expect(runJavaVersion).toHaveBeenCalledTimes(1);
      expect(runJavaVersion).toHaveBeenCalledWith(normalizedConfiguredPath);
    });

    it('verifies the PATH candidate for a null setting', async () => {
      await expect(runtime.verifySetting(null)).resolves.toEqual({
        status: 'ok',
        javaPath: pathCandidate,
        versionOutput: 'openjdk 25'
      });
      expect(findJavaOnPath).toHaveBeenCalledTimes(1);
      expect(findJavaOnPath).toHaveBeenCalledWith();
      expect(runJavaVersion).toHaveBeenCalledTimes(1);
      expect(runJavaVersion).toHaveBeenCalledWith(pathCandidate);
      expect(normalizeToJavaBinary).not.toHaveBeenCalled();
    });

    it('reports no java found on PATH without running -version', async () => {
      findJavaOnPath.mockReturnValue(null);

      await expect(runtime.verifySetting(null)).resolves.toEqual({status: 'error', message: 'No java found on PATH'});
      expect(runJavaVersion).not.toHaveBeenCalled();
    });
  });

  describe('resolve', () => {
    it('resolves to the configured path when it verifies, spending exactly one probe', async () => {
      await expect(runtime.resolve()).resolves.toBe(normalizedConfiguredPath);

      expect(runJavaVersion).toHaveBeenCalledTimes(1);
      expect(runJavaVersion).toHaveBeenCalledWith(normalizedConfiguredPath);
      expect(findJavaOnPath).not.toHaveBeenCalled();
    });

    it('falls back to the PATH candidate when the configured path fails to verify', async () => {
      runJavaVersion.mockImplementation((binaryPath) => Promise.resolve(
        binaryPath === normalizedConfiguredPath
          ? {status: 'error', message: 'broken'}
          : {status: 'ok', javaPath: binaryPath, versionOutput: 'openjdk 25'}
      ));

      await expect(runtime.resolve()).resolves.toBe(pathCandidate);
      expect(runJavaVersion.mock.calls).toEqual([
        [normalizedConfiguredPath],
        [pathCandidate]
      ]);
    });

    it('resolves to null when both the configured path and the PATH candidate fail', async () => {
      runJavaVersion.mockResolvedValue({status: 'error', message: 'broken'});

      await expect(runtime.resolve()).resolves.toBeNull();
    });

    describe('with no configured path', () => {
      beforeEach(() => {
        config.java = {path: null};
      });

      it('resolves to the PATH candidate, spending exactly one probe', async () => {
        await expect(runtime.resolve()).resolves.toBe(pathCandidate);

        expect(runJavaVersion).toHaveBeenCalledTimes(1);
        expect(runJavaVersion).toHaveBeenCalledWith(pathCandidate);
        expect(normalizeToJavaBinary).not.toHaveBeenCalled();
      });

      it('resolves to null without a second probe when no java is on PATH', async () => {
        findJavaOnPath.mockReturnValue(null);

        await expect(runtime.resolve()).resolves.toBeNull();
        expect(runJavaVersion).not.toHaveBeenCalled();
      });
    });

    describe('with no java key in the config at all', () => {
      beforeEach(() => {
        config.java = undefined;
      });

      it('resolves to the PATH candidate', async () => {
        await expect(runtime.resolve()).resolves.toBe(pathCandidate);
      });
    });
  });
});
