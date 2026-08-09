/** @import {AuthRegistry} from '../config/auth-registry.js' */
/** @import {AuthState} from '../config/auth.js' */
/** @import {ConfigFile, ConfigFileState} from '../config/config-file.js' */
/** @import {FynancialsConfig} from '../config/config-schema.js' */

/**
 * Computes which startup mode the window opens into, from the loaded config plus the auth registry. Consuming the
 * one-shot `configureOnNextStart` flag lives here too: it is read and deleted from the config in the
 * same step that decides the mode, so the flag cannot outlive the run it was written for.
 *
 * `resolve()` is a one-shot snapshot rather than a live view - it consumes that flag on the way, so a second call is
 * not equivalent to the first. What it reports describes the database the app started against, and stays true for
 * the whole run on its own terms: a failed start leaves the config untouched, and a proven start only ever fills a
 * pending entry.
 */

/** @typedef {'boot' | 'configure' | 'unlock'} StartupMode */

/**
 * @typedef {Object} StartupState
 * @property {AuthState | null} authState null when the config names no database
 * @property {string | null} databasePath database base path without extension, null when the config names none
 * @property {StartupMode} mode
 */

/**
 * @typedef {Object} StartupModeOptions
 * @property {Pick<ConfigFile, 'save'>} configFile
 * @property {ConfigFileState} configFileState what the single `load()` at start observed about the file
 * @property {FynancialsConfig} config
 * @property {Pick<AuthRegistry, 'stateOf'>} authRegistry
 */

/** @typedef {{resolve: () => StartupState}} StartupModeResolver */

/**
 * @param {StartupModeOptions} options
 * @returns {StartupModeResolver}
 */
function createStartupMode(options) {
  const {configFile, configFileState, config, authRegistry} = options;

  /**
   * @returns {StartupState}
   */
  function resolve() {
    /** @type {string | null} */
    const databasePath = config.env.FY_DB_FILE_PATH ?? null;
    /** @type {AuthState | null} */
    const authState = databasePath == null ? null : authRegistry.stateOf(databasePath);

    // first, so that no ordering of later edits can make a config file we failed to read get written to. The two
    // branches can never both apply - a file that could not be read yields the default, which carries no flag -
    // so the order costs nothing and makes the one branch below that writes unreachable for such a file.
    if (configFileState !== 'read') {
      return {authState, databasePath, mode: 'configure'};
    }

    if (config.configureOnNextStart === true) {
      delete config.configureOnNextStart;
      configFile.save(config);
      return {authState, databasePath, mode: 'configure'};
    }

    if (databasePath == null) {
      return {authState, databasePath, mode: 'configure'};
    }

    return {authState, databasePath, mode: authState === 'passwordless' ? 'boot' : 'unlock'};
  }

  return {resolve};
}

module.exports = {createStartupMode};
