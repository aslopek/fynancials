const {contextBridge, ipcRenderer} = require('electron');

/** @import {BackendStartOutcome} from './backend/backend-process.js' */
/** @import {StartupState} from './window/startup-mode.js' */

// Channel names are literals here on purpose: a sandboxed preload's `require` is a limited polyfill that resolves
// `electron` and a handful of Node built-ins only - it cannot require a module of this app, so `ipc/` cannot be
// shared with it. All four literals are duplicated in `ipc/startup-bridge.js` (`startup:getState`, `backend:start`,
// `auth:verify`, `app:quit`), and the manual checklist (`electron/LLM.md`) is what keeps them in step.
contextBridge.exposeInMainWorld('fynancials', {
  /** @returns {Promise<StartupState>} */
  getStartupState: () => ipcRenderer.invoke('startup:getState'),

  /**
   * @param {string | undefined} [password]
   * @returns {Promise<BackendStartOutcome>}
   */
  startBackend: (password) => ipcRenderer.invoke('backend:start', password),

  /**
   * @param {string} password
   * @returns {Promise<boolean>}
   */
  verifyPassword: (password) => ipcRenderer.invoke('auth:verify', password),

  /** @returns {void} */
  quit: () => ipcRenderer.send('app:quit')
});
