/** @import {AuthRegistry} from '../config/auth-registry.js' */
/** @import {AuthState} from '../config/auth.js' */
/** @import {ConfigFile} from '../config/config-file.js' */
/** @import {FynancialsConfig} from '../config/config-schema.js' */

/**
 * Computes which startup mode the window opens into, from the loaded config plus the auth registry. Consuming the
 * one-shot `configureOnNextStart` flag lives here too (epic ADR-006): it is read and deleted from the config in the
 * same step that decides the mode, before the window renders anything, so no way of leaving the configuration screen
 * can ever leave it dangling.
 */

/** @typedef {'boot' | 'configure' | 'unlock'} StartupMode */

/**
 * @typedef {Object} StartupState
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

    if (config.configureOnNextStart === true) {
      delete config.configureOnNextStart;
      configFile.save(config);
      return {databasePath, mode: 'configure'};
    }

    if (!configFile.exists()) {
      return {databasePath, mode: 'configure'};
    }

    if (databasePath == null) {
      return {databasePath, mode: 'configure'};
    }

    /** @type {AuthState} */
    const authState = authRegistry.stateOf(databasePath);
    return {databasePath, mode: authState === 'passwordless' ? 'boot' : 'unlock'};
  }

  return {resolve};
}

module.exports = {createStartupMode};
