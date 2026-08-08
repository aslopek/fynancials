const path = require('node:path');
const {fynancialsConfigSchema} = require('./config-schema.js');

/** @import {FynancialsConfig} from './config-schema.js' */

/**
 * Reads and writes `fynancials.config.json`. The file system and the file's location are injected, so this module is
 * exercisable without touching a real disk.
 *
 * A missing file returns the default configuration without writing it - the caller (the startup-mode computation)
 * is what decides a missing file means `configure` mode, and that mode must not leave a write behind. An unreadable,
 * unparsable or schema-violating file falls back to the default and overwrites it rather than crashing the app on
 * startup; a failed write is logged, never thrown.
 */

/**
 * The three functions this module needs from `fs` - declared minimally, so that a test stub made of exactly these
 * three `jest.fn()`s is assignable without a cast, and a stub that drifts from the real dependency fails `tsc`.
 *
 * @typedef {Object} ConfigFileSystem
 * @property {(path: string) => boolean} existsSync
 * @property {(path: string, encoding: 'utf-8') => string} readFileSync
 * @property {(path: string, data: string, options: {flag: string}) => void} writeFileSync
 */

/**
 * @typedef {Object} ConfigFileOptions
 * @property {ConfigFileSystem} fileSystem
 * @property {string} configFilePath
 * @property {string} homeDirectory
 * @property {Pick<Console, 'error'>} [logger]
 */

/**
 * @typedef {Object} ConfigFile
 * @property {string} path
 * @property {() => FynancialsConfig} defaultConfig
 * @property {() => boolean} exists
 * @property {() => FynancialsConfig} load
 * @property {(config: FynancialsConfig) => void} save
 */

/**
 * @param {ConfigFileOptions} options
 * @returns {ConfigFile}
 */
function createConfigFile(options) {
  const {fileSystem, configFilePath, homeDirectory} = options;
  const logger = options.logger ?? console;

  /**
   * @returns {FynancialsConfig}
   */
  function defaultConfig() {
    return {
      env: {FY_DB_FILE_PATH: path.join(homeDirectory, 'fynancials')},
      auth: {}
    };
  }

  /**
   * @param {FynancialsConfig} config
   * @returns {void}
   */
  function save(config) {
    try {
      fileSystem.writeFileSync(configFilePath, JSON.stringify(config, null, 2), {flag: 'w'});
    } catch (error) {
      logger.error(`Failed to save config to ${configFilePath}:`, error);
    }
  }

  /**
   * @param {unknown} reason
   * @returns {FynancialsConfig}
   */
  function fallBackToDefault(reason) {
    logger.error(`Failed to read config from ${configFilePath}, falling back to defaults:`, reason);
    const config = defaultConfig();
    save(config);
    return config;
  }

  /**
   * @returns {boolean}
   */
  function exists() {
    return fileSystem.existsSync(configFilePath);
  }

  /**
   * @returns {FynancialsConfig}
   */
  function load() {
    if (!exists()) {
      return defaultConfig();
    }

    let contents;
    try {
      contents = JSON.parse(fileSystem.readFileSync(configFilePath, 'utf-8'));
    } catch (error) {
      return fallBackToDefault(error);
    }

    const parsedConfig = fynancialsConfigSchema.safeParse(contents);
    return parsedConfig.success ? parsedConfig.data : fallBackToDefault(parsedConfig.error);
  }

  return {
    path: configFilePath,
    defaultConfig,
    exists,
    load,
    save
  };
}

module.exports = {createConfigFile};
