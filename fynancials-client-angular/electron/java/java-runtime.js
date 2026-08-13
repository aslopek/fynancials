/** @import {FynancialsConfig} from '../config/config-schema.js' */
/** @import {JavaVerification} from './java-version.js' */

/**
 * The chain that decides which java the app boots with, and the literal check the configuration screen shows for one
 * setting. Both walk the same three collaborators - finding a `PATH` candidate, normalizing a picked path to a
 * binary, and running `-version` against a resolved binary - so `resolve()` is written as `verifySetting` applied
 * twice rather than as a second traversal of the same chain.
 */

/**
 * @typedef {Object} JavaRuntimeOptions
 * @property {FynancialsConfig} config read live, so a later re-resolution after a config write sees the fresh path
 * @property {() => string | null} findJavaOnPath the absolute `PATH` candidate, or null when there is none
 * @property {(pickedPath: string) => string} normalizeToJavaBinary
 * @property {(binaryPath: string) => Promise<JavaVerification>} runJavaVersion
 */

/**
 * @typedef {Object} JavaRuntime
 * @property {() => Promise<string | null>} resolve
 * @property {(setting: string | null) => Promise<JavaVerification>} verifySetting
 */

/**
 * @param {JavaRuntimeOptions} options
 * @returns {JavaRuntime}
 */
function createJavaRuntime(options) {
  const {config, findJavaOnPath, normalizeToJavaBinary, runJavaVersion} = options;

  /**
   * Verifies exactly the setting handed in, with no fallback - `null` means the `PATH` candidate. This is what makes a stale `java.path`
   * visible even on a machine that would otherwise boot fine.
   *
   * @param {string | null} setting the configured `java.path` - an absolute path to a JDK home directory or directly to a
   *   `java`/`java.exe` binary, or `null` when none is configured, in which case the `PATH` candidate decides
   * @returns {Promise<JavaVerification>}
   */
  async function verifySetting(setting) {
    if (setting == null) {
      const candidate = findJavaOnPath();
      if (candidate == null) {
        return {status: 'error', message: 'No java found on PATH'};
      }
      return runJavaVersion(candidate);
    }
    return runJavaVersion(normalizeToJavaBinary(setting));
  }

  /**
   * The configured setting wins when it verifies; only its failure spends a second probe on the `PATH` candidate -
   * an already-automatic setting needs no second probe, since `verifySetting(null)` already is that check.
   *
   * @returns {Promise<string | null>}
   */
  async function resolve() {
    /** @type {string | null} */
    const settingPath = config.java?.path ?? null;
    const primary = await verifySetting(settingPath);
    if (primary.status === 'ok') {
      return primary.javaPath;
    }
    if (settingPath == null) {
      return null;
    }
    const fallback = await verifySetting(null);
    return fallback.status === 'ok' ? fallback.javaPath : null;
  }

  return {resolve, verifySetting};
}

module.exports = {createJavaRuntime};
