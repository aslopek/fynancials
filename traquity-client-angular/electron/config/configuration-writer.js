/** @import {AuthRegistry} from './auth-registry.js' */
/** @import {AuthState} from './auth.js' */
/** @import {ConfigFile} from './config-file.js' */
/** @import {TraQuityConfig} from './config-schema.js' */

/**
 * The single config write applying a finished configuration: each configurable setting becomes one key of
 * `traquity.config.json`, and all of them are persisted in one `save` - never a key at a time.
 *
 * Two keys are deliberately out of reach here. `auth` is never written: a created database stays pending until its
 * backend start proves the password, which is `authRegistry.recordProvenStart`'s job. And
 * `configureOnNextStart` is consumed while the startup mode is resolved, so by the time a
 * configuration can be applied there is nothing left to clear.
 */

/**
 * @typedef {Object} ConfigurationChanges
 * @property {string} databasePath database base path without extension
 * @property {string | null} javaPath the verified custom binary, null for the automatically resolved one
 * @property {string | null} javaSignature base64 of a downloaded archive's detached signature, null when the path was
 *   not written by a download this app performed
 */

/**
 * @typedef {Object} ConfigurationWriter
 * @property {(changes: ConfigurationChanges) => AuthState} apply
 */

/**
 * @typedef {Object} ConfigurationWriterOptions
 * @property {Pick<ConfigFile, 'save'>} configFile
 * @property {TraQuityConfig} config
 * @property {Pick<AuthRegistry, 'stateOf'>} authRegistry
 */

/**
 * @param {ConfigurationWriterOptions} options
 * @returns {ConfigurationWriter}
 */
function createConfigurationWriter(options) {
  const {configFile, config, authRegistry} = options;

  /**
   * @param {ConfigurationChanges} changes
   * @returns {AuthState} the state of the newly selected database, as the registry knows it after the write
   */
  function apply(changes) {
    config.env.TQ_DB_FILE_PATH = changes.databasePath;
    config.java = {...config.java, path: changes.javaPath, signature: changes.javaSignature};
    configFile.save(config);
    return authRegistry.stateOf(changes.databasePath);
  }

  return {apply};
}

module.exports = {createConfigurationWriter};
