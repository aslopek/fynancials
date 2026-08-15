/** @import {ConfigFile} from './config-file.js' */
/** @import {FynancialsConfig} from './config-schema.js' */

/**
 * Sets `configureOnNextStart` on the loaded configuration object and saves that object as it stands, so every other
 * key reaches the disk exactly as it was loaded. Nothing is re-read and no partial object is built.
 */

/**
 * @typedef {Object} ConfigureOnNextStart
 * @property {() => void} request
 */

/**
 * @typedef {Object} ConfigureOnNextStartOptions
 * @property {Pick<ConfigFile, 'save'>} configFile
 * @property {FynancialsConfig} config
 */

/**
 * @param {ConfigureOnNextStartOptions} options
 * @returns {ConfigureOnNextStart}
 */
function createConfigureOnNextStart(options) {
  const {configFile, config} = options;

  /**
   * @returns {void}
   */
  function request() {
    config.configureOnNextStart = true;
    configFile.save(config);
  }

  return {request};
}

module.exports = {createConfigureOnNextStart};
