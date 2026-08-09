const {
  authVerifyPasswordSchema,
  backendStartPasswordSchema,
  configurationChangesSchema,
  databasePathSchema,
  databaseSelectionSchema
} = require('./ipc-schema.js');

/** @import {AuthRegistry, KnownDatabase} from '../config/auth-registry.js' */
/** @import {AuthState} from '../config/auth.js' */
/** @import {BackendProcess, BackendStartOutcome} from '../backend/backend-process.js' */
/** @import {ConfigFileState} from '../config/config-file.js' */
/** @import {ConfigurationWriter} from '../config/configuration-writer.js' */
/** @import {DatabaseDialogs} from '../window/database-dialogs.js' */
/** @import {FynancialsConfig} from '../config/config-schema.js' */
/** @import {StartupState} from '../window/startup-mode.js' */

/**
 * Registers the IPC channels the preload's `contextBridge` surface calls into. Exactly nine channels, no generic
 * `invoke(channel, ...)` passthrough, ever. A wider surface would let the renderer reach into the main process in
 * uncontrolled manner. Eight are request/response (registered via `ipcMain.handle`); one is one-way
 * (`app:quit`, registered via `ipcMain.on`) because it has no answer to give.
 *
 * Exactly two of them write `fynancials.config.json`, and neither can write more than its own key: `auth:forget`
 * removes one `auth` entry through `authRegistry.forget`, `config:apply` sets `env.FY_DB_FILE_PATH` through
 * `configurationWriter.apply`. Nothing here ever *writes* an `auth` entry: recording one is a proven start's job.
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
 * What `configure:getState` answers with, beyond `StartupState`. `configFileState` is the snapshot of the single
 * `load()` at start, deliberately not re-derived: `auth:forget` writes the config file later in the run, and this
 * value has to keep describing what the start observed rather than what the file looks like now.
 *
 * @typedef {Object} ConfigureState
 * @property {ConfigFileState} configFileState
 * @property {KnownDatabase[]} knownDatabases
 * @property {string} logPath where the main process writes `fynancials.log`
 */

/**
 * @typedef {Object} AppliedConfiguration
 * @property {string} databasePath
 * @property {AuthState} authState
 */

/**
 * @typedef {Object} StartupBridgeOptions
 * @property {IpcMainLike} ipcMain
 * @property {StartupState} startupState
 * @property {ConfigFileState} configFileState
 * @property {Pick<BackendProcess, 'start'>} backendProcess
 * @property {Pick<AuthRegistry, 'verify' | 'knownDatabases' | 'forget'>} authRegistry
 * @property {Pick<ConfigurationWriter, 'apply'>} configurationWriter
 * @property {DatabaseDialogs} databaseDialogs
 * @property {FynancialsConfig} config
 * @property {string} logPath
 * @property {() => void} quit
 */

/**
 * @param {StartupBridgeOptions} options
 * @returns {{register: () => void}}
 */
function createStartupBridge(options) {
  const {
    ipcMain,
    startupState,
    configFileState,
    backendProcess,
    authRegistry,
    configurationWriter,
    databaseDialogs,
    config,
    logPath,
    quit
  } = options;

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

    ipcMain.handle('configure:getState', () => {
      /** @type {ConfigureState} */
      const configureState = {
        configFileState,
        knownDatabases: authRegistry.knownDatabases(),
        logPath
      };
      return configureState;
    });

    ipcMain.handle('database:pickExisting', (_event, currentSelection) => {
      const parsedSelection = databaseSelectionSchema.safeParse(currentSelection);
      if (!parsedSelection.success) {
        throw new Error('Invalid currentSelection argument for database:pickExisting');
      }
      return databaseDialogs.pickExisting(parsedSelection.data);
    });

    ipcMain.handle('database:pickNew', (_event, currentSelection) => {
      const parsedSelection = databaseSelectionSchema.safeParse(currentSelection);
      if (!parsedSelection.success) {
        throw new Error('Invalid currentSelection argument for database:pickNew');
      }
      return databaseDialogs.pickNew(parsedSelection.data);
    });

    ipcMain.handle('auth:forget', (_event, databasePath) => {
      const parsedDatabasePath = databasePathSchema.safeParse(databasePath);
      if (!parsedDatabasePath.success) {
        throw new Error('Invalid databasePath argument for auth:forget');
      }
      authRegistry.forget(parsedDatabasePath.data);
    });

    ipcMain.handle('config:apply', (_event, changes) => {
      const parsedChanges = configurationChangesSchema.safeParse(changes);
      if (!parsedChanges.success) {
        throw new Error('Invalid changes argument for config:apply');
      }
      /** @type {AppliedConfiguration} */
      const applied = {
        databasePath: parsedChanges.data.databasePath,
        authState: configurationWriter.apply(parsedChanges.data)
      };
      return applied;
    });

    ipcMain.on('app:quit', () => quit());
  }

  return {register};
}

module.exports = {createStartupBridge};
