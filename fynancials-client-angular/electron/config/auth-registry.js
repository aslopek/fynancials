const {authStateOf, createScryptRecord, passwordlessEntry, verifyPassword} = require('./auth.js');

/** @import {AuthState} from './auth.js' */
/** @import {ConfigFile} from './config-file.js' */
/** @import {FynancialsConfig} from './config-schema.js' */

/**
 * The `auth` map of a loaded config, keyed by database base path, with no normalization and no case folding: moving or renaming a database
 * file outside the app strands its entry.
 */

/**
 * @typedef {Object} AuthRegistry
 * @property {(databasePath: string) => AuthState} stateOf
 * @property {(databasePath: string, candidate: string) => boolean} verify
 * @property {(databasePath: string, password: string) => void} recordProvenStart
 */

/**
 * @typedef {Object} AuthRegistryOptions
 * @property {Pick<ConfigFile, 'save'>} configFile
 * @property {FynancialsConfig} config
 */

/**
 * @param {AuthRegistryOptions} options
 * @returns {AuthRegistry}
 */
function createAuthRegistry(options) {
  const {configFile, config} = options;

  /**
   * @param {string} databasePath
   * @returns {AuthState}
   */
  function stateOf(databasePath) {
    return authStateOf(config.auth[databasePath]);
  }

  /**
   * @param {string} databasePath
   * @param {string} candidate
   * @returns {boolean}
   */
  function verify(databasePath, candidate) {
    return verifyPassword(config.auth[databasePath], candidate);
  }

  /**
   * Captures the password state of a database once the backend spawned with that password has proven itself
   * reachable - which is when the H2 file itself has confirmed the password, the only authority there is.
   *
   * Only a pending database is ever written: an existing, non-pending entry is left untouched, so a start failure cannot discard
   * a record and a proven start cannot rewrite one.
   *
   * @param {string} databasePath
   * @param {string} password
   * @returns {void}
   */
  function recordProvenStart(databasePath, password) {
    if (stateOf(databasePath) !== 'pending') {
      return;
    }
    config.auth[databasePath] = password === '' ? passwordlessEntry() : createScryptRecord(password);
    configFile.save(config);
  }

  return {
    stateOf,
    verify,
    recordProvenStart
  };
}

module.exports = {createAuthRegistry};
