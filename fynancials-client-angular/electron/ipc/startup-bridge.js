const {backendStartPasswordSchema} = require('./ipc-schema.js');

/** @import {BackendProcess, BackendStartOutcome} from '../backend/backend-process.js' */
/** @import {StartupState} from '../window/startup-mode.js' */

/**
 * Registers the IPC channels the preload's `contextBridge` surface calls into. Exactly two channels, no generic
 * `invoke(channel, ...)` passthrough, ever - a wider surface would let the renderer reach into the main process in
 * ways nothing here has reviewed.
 */

/**
 * `require('electron')` is not loadable under jest (`testEnvironment: 'node'`), so this module must not import it.
 * Electron's real `ipcMain` is structurally assignable to this minimal shape.
 *
 * @typedef {Object} IpcMainLike
 * @property {(channel: string, listener: (event: unknown, ...args: unknown[]) => unknown) => void} handle
 */

/**
 * @typedef {Object} StartupBridgeOptions
 * @property {IpcMainLike} ipcMain
 * @property {StartupState} startupState
 * @property {Pick<BackendProcess, 'start'>} backendProcess
 */

/**
 * @param {StartupBridgeOptions} options
 * @returns {{register: () => void}}
 */
function createStartupBridge(options) {
  const {ipcMain, startupState, backendProcess} = options;

  /**
   * @returns {void}
   */
  function register() {
    ipcMain.handle('startup:getState', () => startupState);
    ipcMain.handle('backend:start', (_event, password) => {
      const parsedPassword = backendStartPasswordSchema.safeParse(password);
      if (!parsedPassword.success) {
        throw new Error('Invalid password argument for backend:start');
      }
      return backendProcess.start(parsedPassword.data ?? '');
    });
  }

  return {register};
}

module.exports = {createStartupBridge};
