const {beforeEach, describe, expect, it, jest} = require('@jest/globals');
const {createStartupBridge} = require('./startup-bridge.js');

/** @import {AuthRegistry, KnownDatabase} from '../config/auth-registry.js' */
/** @import {AuthState} from '../config/auth.js' */
/** @import {BackendProcess, BackendStartOutcome} from '../backend/backend-process.js' */
/** @import {ConfigurationChanges, ConfigurationWriter} from '../config/configuration-writer.js' */
/** @import {DatabaseDialogs, PickedDatabase} from '../window/database-dialogs.js' */
/** @import {FynancialsConfig} from '../config/config-schema.js' */
/** @import {StartupState} from '../window/startup-mode.js' */
/** @import {IpcMainLike} from './startup-bridge.js' */

describe('startupBridge', () => {
  const databasePath = 'C:\\Users\\x\\fynancials';
  const otherDatabasePath = 'D:\\backup\\fynancials-test';
  const logPath = 'C:\\apps\\fynancials\\fynancials.log';

  /** @type {StartupState} */
  let startupState;

  /** @type {KnownDatabase[]} */
  let known;

  const handle = jest.fn(/** @type {IpcMainLike['handle']} */ (() => {
  }));
  const on = jest.fn(/** @type {IpcMainLike['on']} */ (() => {
  }));
  const start = jest.fn(/** @type {(password: string) => Promise<BackendStartOutcome>} */
    (() => Promise.resolve({reachable: true, startedFrom: 'pending'})));
  const verify = jest.fn(/** @type {(databasePath: string, candidate: string) => boolean} */ (() => true));
  const knownDatabases = jest.fn(/** @type {() => KnownDatabase[]} */ (() => known));
  const forget = jest.fn(/** @type {(databasePath: string) => void} */ (() => undefined));
  const apply = jest.fn(/** @type {(changes: ConfigurationChanges) => AuthState} */ (() => 'passwordless'));
  const pickExisting = jest.fn(/** @type {DatabaseDialogs['pickExisting']} */
    (() => Promise.resolve(otherDatabasePath)));
  const pickNew = jest.fn(/** @type {DatabaseDialogs['pickNew']} */
    (() => Promise.resolve({basePath: otherDatabasePath, fileExists: false})));
  const quit = jest.fn();

  /** @type {IpcMainLike} */
  let ipcMain;

  /** @type {Pick<BackendProcess, 'start'>} */
  let backendProcess;

  /** @type {Pick<AuthRegistry, 'verify' | 'knownDatabases' | 'forget'>} */
  let authRegistry;

  /** @type {Pick<ConfigurationWriter, 'apply'>} */
  let configurationWriter;

  /** @type {DatabaseDialogs} */
  let databaseDialogs;

  /** @type {FynancialsConfig} */
  let config;

  /**
   * @param {string} channel
   * @returns {(event: unknown, ...args: unknown[]) => unknown}
   */
  function handleListenerFor(channel) {
    const call = handle.mock.calls.find(([registeredChannel]) => registeredChannel === channel);
    if (call == null) {
      throw new Error(`No handler registered for ${channel}`);
    }
    return call[1];
  }

  /**
   * @param {string} channel
   * @returns {(event: unknown, ...args: unknown[]) => void}
   */
  function onListenerFor(channel) {
    const call = on.mock.calls.find(([registeredChannel]) => registeredChannel === channel);
    if (call == null) {
      throw new Error(`No 'on' listener registered for ${channel}`);
    }
    return call[1];
  }

  beforeEach(() => {
    startupState = {authState: 'scrypt', databasePath, mode: 'unlock'};
    known = [
      {
        path: databasePath,
        authState: 'scrypt'
      }
    ];

    jest.clearAllMocks();
    start.mockResolvedValue({reachable: true, startedFrom: 'pending'});
    verify.mockReturnValue(true);
    knownDatabases.mockReturnValue(known);
    apply.mockReturnValue('passwordless');
    pickExisting.mockResolvedValue(otherDatabasePath);
    pickNew.mockResolvedValue({basePath: otherDatabasePath, fileExists: false});

    ipcMain = {handle, on};
    backendProcess = {start};
    authRegistry = {verify, knownDatabases, forget};
    configurationWriter = {apply};
    databaseDialogs = {pickExisting, pickNew};
    config = {
      env: {FY_DB_FILE_PATH: databasePath},
      auth: {}
    };

    createStartupBridge({
      ipcMain,
      startupState,
      configFileState: 'read',
      backendProcess,
      authRegistry,
      configurationWriter,
      databaseDialogs,
      config,
      logPath,
      quit
    }).register();
  });

  it('registers exactly the eight request/response channels via handle', () => {
    expect(handle.mock.calls.map(([channel]) => channel)).toEqual([
      'startup:getState',
      'backend:start',
      'auth:verify',
      'configure:getState',
      'database:pickExisting',
      'database:pickNew',
      'auth:forget',
      'config:apply'
    ]);
  });

  it('registers exactly the app:quit channel via on', () => {
    expect(on.mock.calls.map(([channel]) => channel)).toEqual(['app:quit']);
  });

  it('resolves startup:getState with the given startup state', () => {
    expect(handleListenerFor('startup:getState')(undefined)).toBe(startupState);
  });

  it('delegates a string password to backendProcess.start', async () => {
    await handleListenerFor('backend:start')(undefined, 'hunter2');

    expect(start).toHaveBeenCalledWith('hunter2');
  });

  it('delegates an empty password when none is given', async () => {
    await handleListenerFor('backend:start')(undefined);

    expect(start).toHaveBeenCalledWith('');
  });

  it('rejects a non-string password without reaching backendProcess.start', () => {
    expect(() => handleListenerFor('backend:start')(undefined, 42)).toThrow('Invalid password argument for backend:start');
    expect(start).not.toHaveBeenCalled();
  });

  it('propagates a rejection from backendProcess.start', async () => {
    start.mockRejectedValue(new Error('A backend is already running'));

    await expect(handleListenerFor('backend:start')(undefined, 'hunter2')).rejects.toThrow('A backend is already running');
  });

  it('delegates auth:verify to authRegistry.verify with the config database path and returns its result', () => {
    expect(handleListenerFor('auth:verify')(undefined, 'hunter2')).toBe(true);
    expect(verify).toHaveBeenCalledWith(databasePath, 'hunter2');
  });

  it('returns false from auth:verify when authRegistry.verify rejects the password', () => {
    verify.mockReturnValue(false);

    expect(handleListenerFor('auth:verify')(undefined, 'hunter2')).toBe(false);
  });

  describe('when the config names no database', () => {
    beforeEach(() => {
      config.env = {};
    });

    it('returns false from auth:verify without calling authRegistry.verify', () => {
      expect(handleListenerFor('auth:verify')(undefined, 'hunter2')).toBe(false);
      expect(verify).not.toHaveBeenCalled();
    });
  });

  it('rejects a non-string password for auth:verify without reaching authRegistry.verify', () => {
    expect(() => handleListenerFor('auth:verify')(undefined, 42)).toThrow('Invalid password argument for auth:verify');
    expect(verify).not.toHaveBeenCalled();
  });

  it('rejects a missing password for auth:verify without reaching authRegistry.verify', () => {
    expect(() => handleListenerFor('auth:verify')(undefined)).toThrow('Invalid password argument for auth:verify');
    expect(verify).not.toHaveBeenCalled();
  });

  it('resolves configure:getState with the read outcome, the known databases and the log path', () => {
    expect(handleListenerFor('configure:getState')(undefined)).toEqual({
      configFileState: 'read',
      knownDatabases: known,
      logPath
    });
  });

  it('delegates database:pickExisting to the dialogs and returns the picked base path', async () => {
    await expect(handleListenerFor('database:pickExisting')(undefined, databasePath)).resolves.toBe(otherDatabasePath);
    expect(pickExisting).toHaveBeenCalledWith(databasePath);
  });

  it('passes a null selection to database:pickExisting', async () => {
    await handleListenerFor('database:pickExisting')(undefined, null);

    expect(pickExisting).toHaveBeenCalledWith(null);
  });

  it('rejects a non-string selection for database:pickExisting without opening a dialog', () => {
    expect(() => handleListenerFor('database:pickExisting')(undefined, 42))
      .toThrow('Invalid currentSelection argument for database:pickExisting');
    expect(pickExisting).not.toHaveBeenCalled();
  });

  it('delegates database:pickNew to the dialogs and returns the picked database', async () => {
    await expect(handleListenerFor('database:pickNew')(undefined, databasePath)).resolves.toEqual({
      basePath: otherDatabasePath,
      fileExists: false
    });
    expect(pickNew).toHaveBeenCalledWith(databasePath);
  });

  it('rejects a non-string selection for database:pickNew without opening a dialog', () => {
    expect(() => handleListenerFor('database:pickNew')(undefined, 42))
      .toThrow('Invalid currentSelection argument for database:pickNew');
    expect(pickNew).not.toHaveBeenCalled();
  });

  it('delegates auth:forget to the registry', () => {
    handleListenerFor('auth:forget')(undefined, databasePath);

    expect(forget).toHaveBeenCalledTimes(1);
    expect(forget).toHaveBeenCalledWith(databasePath);
  });

  it('rejects an empty database path for auth:forget without reaching the registry', () => {
    expect(() => handleListenerFor('auth:forget')(undefined, '')).toThrow('Invalid databasePath argument for auth:forget');
    expect(forget).not.toHaveBeenCalled();
  });

  it('applies the changes for config:apply and reports the selected database with its state', () => {
    expect(handleListenerFor('config:apply')(undefined, {databasePath: otherDatabasePath})).toEqual({
      databasePath: otherDatabasePath,
      authState: 'passwordless'
    });
    expect(apply).toHaveBeenCalledWith({databasePath: otherDatabasePath});
  });

  it('rejects changes carrying an unknown key for config:apply without applying anything', () => {
    expect(() => handleListenerFor('config:apply')(undefined, {databasePath: otherDatabasePath, auth: {}}))
      .toThrow('Invalid changes argument for config:apply');
    expect(apply).not.toHaveBeenCalled();
  });

  // the two dialog channels reach no writing collaborator at all - there is none among the ones they can call
  it('writes nothing when a dialog is opened', async () => {
    await handleListenerFor('database:pickExisting')(undefined, databasePath);
    await handleListenerFor('database:pickNew')(undefined, databasePath);

    expect(forget).not.toHaveBeenCalled();
    expect(apply).not.toHaveBeenCalled();
  });

  it('calls the injected quit for app:quit and spawns no backend and verifies no password', () => {
    onListenerFor('app:quit')(undefined);

    expect(quit).toHaveBeenCalledTimes(1);
    expect(start).not.toHaveBeenCalled();
    expect(verify).not.toHaveBeenCalled();
  });
});
