const {fynancialsConfigSchema} = require('./config-schema.js');

/** @import {FynancialsConfig} from './config-schema.js' */

/**
 * Reads and writes `fynancials.config.json`. The file system and the file's location are injected, so this module is
 * exercisable without touching a real disk.
 *
 * `load()` never writes, in any of its three outcomes. A missing file and an unreadable one - unparsable JSON, a
 * schema violation, a failed read - both yield the default configuration in memory, so the rest of the main process
 * has something well-formed to work with, while the file on disk is left exactly as it is.
 *
 * The default configuration deliberately names no database: a proposed path would be one save away from becoming a
 * decision the user never made, so the absence stays explicit and has to be answered before a database is opened.
 */

/**
 * What `load()` observed about the file on disk.
 *
 * @typedef {'read' | 'missing' | 'unreadable'} ConfigFileState
 */

/**
 * @typedef {Object} LoadedConfig
 * @property {FynancialsConfig} config the parsed configuration, or the default one when it could not be read
 * @property {ConfigFileState} state
 */

/**
 * The four functions this module needs from `fs` - declared minimally, so that a test stub made of exactly these
 * four `jest.fn()`s is assignable without a cast, and a stub that drifts from the real dependency fails `tsc`.
 *
 * @typedef {Object} ConfigFileSystem
 * @property {(path: string) => boolean} existsSync
 * @property {(path: string, encoding: 'utf-8') => string} readFileSync
 * @property {(path: string, data: string, options: {flag: string, mode: number}) => void} writeFileSync
 * @property {(path: string, mode: number) => void} chmodSync
 */

/**
 * @typedef {Object} ConfigFileOptions
 * @property {ConfigFileSystem} fileSystem
 * @property {string} configFilePath
 * @property {Pick<Console, 'error'>} [logger]
 */

/**
 * @typedef {Object} ConfigFile
 * @property {string} path
 * @property {() => LoadedConfig} load
 * @property {(config: FynancialsConfig) => void} save
 */

/**
 * @param {ConfigFileOptions} options
 * @returns {ConfigFile}
 */
function createConfigFile(options) {
  const {fileSystem, configFilePath} = options;
  const logger = options.logger ?? console;

  /**
   * @returns {FynancialsConfig}
   */
  function defaultConfig() {
    return {
      env: {},
      auth: {}
    };
  }

  /**
   * The mode a `chmod` sets on the file after every write, and the one it is created with: readable and writable by
   * its owner only. The file carries the scrypt record of a database password, and a hash that can be read can be
   * attacked offline for as long as the attacker likes - a default umask leaves it readable to every local account.
   *
   * @type {number}
   */
  const CONFIG_FILE_MODE = 0o600;

  /**
   * A `mode` passed to a write only applies to a file being created, so the `chmod` is what covers a file that is
   * already there - written by an earlier version, or restored from a backup. It runs after the write and reports its
   * own failure: a config that could not be narrowed is still a config that was saved.
   *
   * @returns {void}
   */
  function restrictToOwner() {
    try {
      fileSystem.chmodSync(configFilePath, CONFIG_FILE_MODE);
    } catch (error) {
      logger.error(`Failed to restrict permissions of ${configFilePath}:`, error);
    }
  }

  /**
   * @param {FynancialsConfig} config
   * @returns {void}
   */
  function save(config) {
    try {
      fileSystem.writeFileSync(configFilePath, JSON.stringify(config, null, 2), {flag: 'w', mode: CONFIG_FILE_MODE});
    } catch (error) {
      logger.error(`Failed to save config to ${configFilePath}:`, error);
      return;
    }
    restrictToOwner();
  }

  /**
   * @param {unknown} reason
   * @returns {LoadedConfig}
   */
  function fallBackToDefault(reason) {
    logger.error(`Failed to read config from ${configFilePath}, falling back to defaults:`, reason);
    return {config: defaultConfig(), state: 'unreadable'};
  }

  /**
   * @returns {LoadedConfig}
   */
  function load() {
    if (!fileSystem.existsSync(configFilePath)) {
      return {config: defaultConfig(), state: 'missing'};
    }

    let contents;
    try {
      contents = JSON.parse(fileSystem.readFileSync(configFilePath, 'utf-8'));
    } catch (error) {
      return fallBackToDefault(error);
    }

    const parsedConfig = fynancialsConfigSchema.safeParse(contents);
    return parsedConfig.success ? {config: parsedConfig.data, state: 'read'} : fallBackToDefault(parsedConfig.error);
  }

  return {
    path: configFilePath,
    load,
    save
  };
}

module.exports = {createConfigFile};
