const {authVerifyPasswordSchema, backendStartPasswordSchema} = require('./ipc-schema.js');

/** @import {AuthRegistry} from '../config/auth-registry.js' */
/** @import {BackendProcess, BackendStartOutcome} from '../backend/backend-process.js' */
/** @import {FynancialsConfig} from '../config/config-schema.js' */
/** @import {StartupState} from '../window/startup-mode.js' */

/**
 * Registers the IPC channels the preload's `contextBridge` surface calls into. Exactly four channels, no generic
 * `invoke(channel, ...)` passthrough, ever - a wider surface would let the renderer reach into the main process in
 * ways nothing here has reviewed. Three are request/response (`startup:getState`, `backend:start`, `auth:verify`,
 * registered via `ipcMain.handle`); one is one-way (`app:quit`, registered via `ipcMain.on`) because it has no
 * answer to give.
 *
 * The `config` this module holds is read-only in practice: no `ConfigFile` is injected, so no channel registered here
 * can write `fynancials.config.json`. That is what makes "no changes to the config file are written" structural for
 * every renderer-triggered path through this bridge rather than something a spec has to keep watching.
 */

/**
 * `require('electron')` is not loadable under jest (`testEnvironment: 'node'`), so this module must not import it.
 * Electron's real `ipcMain` is structurally assignable to this minimal shape.
 *
 * @typedef {Object} IpcMainLike
 * @property {(channel: string, listener: (event: unknown, ...args: unknown[]) => unknown) => void} handle
 * @property {(channel: string, listener: (event: unknown, ...args: unknown[]) => void) => void} on
 */

/**
 * @typedef {Object} StartupBridgeOptions
 * @property {IpcMainLike} ipcMain
 * @property {StartupState} startupState
 * @property {Pick<BackendProcess, 'start'>} backendProcess
 * @property {Pick<AuthRegistry, 'verify'>} authRegistry
 * @property {FynancialsConfig} config read live, so #37's database switch is picked up without re-registering
 * @property {() => void} quit
 */

/**
 * @param {StartupBridgeOptions} options
 * @returns {{register: () => void}}
 */
function createStartupBridge(options) {
  const {ipcMain, startupState, backendProcess, authRegistry, config, quit} = options;

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

    ipcMain.handle('auth:verify', (_event, password) => {
      const parsedPassword = authVerifyPasswordSchema.safeParse(password);
      if (!parsedPassword.success) {
        throw new Error('Invalid password argument for auth:verify');
      }
      /** @type {string | null} */
      const databasePath = config.env.FY_DB_FILE_PATH ?? null;
      return databasePath != null && authRegistry.verify(databasePath, parsedPassword.data);
    });

    ipcMain.on('app:quit', () => quit());
  }

  return {register};
}

module.exports = {createStartupBridge};
