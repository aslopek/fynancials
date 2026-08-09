const {contextBridge, ipcRenderer} = require('electron');

/** @import {BackendStartOutcome} from './backend/backend-process.js' */
/** @import {ConfigurationChanges} from './config/configuration-writer.js' */
/** @import {AppliedConfiguration, ConfigureState} from './ipc/startup-bridge.js' */
/** @import {PickedDatabase} from './window/database-dialogs.js' */
/** @import {StartupState} from './window/startup-mode.js' */

// Channel names are literals here on purpose: a sandboxed preload's `require` is a limited polyfill that resolves
// `electron` and a handful of Node built-ins only - it cannot require a module of this app, so `ipc/` cannot be
// shared with it. All nine literals are duplicated in `ipc/startup-bridge.js`:
//   - `startup:getState`
//   - `backend:start`
//   - `auth:verify`
//   - `configure:getState`
//   - `database:pickExisting`
//   - `database:pickNew`
//   - `auth:forget`
//   - `config:apply`
//   - `app:quit`
// The manual checklist (`electron/LLM.md`) is what keeps the two copies in step.
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

  /** @returns {Promise<ConfigureState>} */
  getConfigureState: () => ipcRenderer.invoke('configure:getState'),

  /**
   * @param {string | null} currentSelection
   * @returns {Promise<string | null>}
   */
  pickExistingDatabase: (currentSelection) => ipcRenderer.invoke('database:pickExisting', currentSelection),

  /**
   * @param {string | null} currentSelection
   * @returns {Promise<PickedDatabase | null>}
   */
  pickNewDatabase: (currentSelection) => ipcRenderer.invoke('database:pickNew', currentSelection),

  /**
   * @param {string} databasePath
   * @returns {Promise<void>}
   */
  forgetPassword: (databasePath) => ipcRenderer.invoke('auth:forget', databasePath),

  /**
   * @param {ConfigurationChanges} changes
   * @returns {Promise<AppliedConfiguration>}
   */
  applyConfiguration: (changes) => ipcRenderer.invoke('config:apply', changes),

  /** @returns {void} */
  quit: () => ipcRenderer.send('app:quit')
});
