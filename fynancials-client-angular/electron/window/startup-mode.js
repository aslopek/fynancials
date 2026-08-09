/** @import {AuthRegistry} from '../config/auth-registry.js' */
/** @import {AuthState} from '../config/auth.js' */
/** @import {ConfigFile} from '../config/config-file.js' */
/** @import {FynancialsConfig} from '../config/config-schema.js' */

/**
 * Computes which startup mode the window opens into, from the loaded config plus the auth registry. Consuming the
 * one-shot `configureOnNextStart` flag lives here too (epic ADR-006): it is read and deleted from the config in the
 * same step that decides the mode, before the window renders anything, so no way of leaving the configuration screen
 * can ever leave it dangling.
 *
 * `resolve()` runs once, at `app.on('ready')`, and the `authState` it reports is what the bridge hands the renderer
 * for the whole run. That value cannot go stale: a failed start leaves the config untouched (epic ADR-003), and every
 * path that would change it - a proven start, a database switch - leaves the unlock screen for good. There is nothing
 * for a refresh channel to do.
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
 * @property {Pick<ConfigFile, 'exists' | 'save'>} configFile
 * @property {FynancialsConfig} config
 * @property {Pick<AuthRegistry, 'stateOf'>} authRegistry
 */

/** @typedef {{resolve: () => StartupState}} StartupModeResolver */

/**
 * @param {StartupModeOptions} options
 * @returns {StartupModeResolver}
 */
function createStartupMode(options) {
  const {configFile, config, authRegistry} = options;

  /**
   * @returns {StartupState}
   */
  function resolve() {
    /** @type {string | null} */
    const databasePath = config.env.FY_DB_FILE_PATH ?? null;
    /** @type {AuthState | null} */
    const authState = databasePath == null ? null : authRegistry.stateOf(databasePath);

    if (config.configureOnNextStart === true) {
      delete config.configureOnNextStart;
      configFile.save(config);
      return {authState, databasePath, mode: 'configure'};
    }

    if (!configFile.exists()) {
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
