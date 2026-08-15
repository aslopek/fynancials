/** @import {BackendProcess} from '../backend/backend-process.js' */
/** @import {ConfigureOnNextStart} from '../config/configure-on-next-start.js' */

/**
 * @typedef {Object} RestartIntoConfigurationOptions
 * @property {Pick<ConfigureOnNextStart, 'request'>} configureOnNextStart
 * @property {Pick<BackendProcess, 'kill'>} backendProcess
 * @property {Pick<import('electron').App, 'relaunch' | 'exit'>} app
 */

/** @typedef {{restart: () => void}} RestartIntoConfiguration */

/**
 * @param {RestartIntoConfigurationOptions} options
 * @returns {RestartIntoConfiguration}
 */
function createRestartIntoConfiguration(options) {
  const {configureOnNextStart, backendProcess, app} = options;

  /**
   * `app.exit(0)` does not fire `window-all-closed`, so the backend has to be killed here, before it, or the
   * relaunched instance finds the old one still holding the port and the H2 file.
   *
   * @returns {void}
   */
  function restart() {
    configureOnNextStart.request();
    backendProcess.kill();
    app.relaunch();
    app.exit(0);
  }

  return {restart};
}

module.exports = {createRestartIntoConfiguration};
