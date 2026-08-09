const {beforeEach, describe, expect, it, jest} = require('@jest/globals');
const {createStartupBridge} = require('./startup-bridge.js');

/** @import {AuthRegistry} from '../config/auth-registry.js' */
/** @import {AuthState} from '../config/auth.js' */
/** @import {BackendProcess, BackendStartOutcome} from '../backend/backend-process.js' */
/** @import {FynancialsConfig} from '../config/config-schema.js' */
/** @import {StartupState} from '../window/startup-mode.js' */
/** @import {IpcMainLike} from './startup-bridge.js' */

describe('startupBridge', () => {
  const databasePath = 'C:\\Users\\x\\fynancials';

  /** @type {StartupState} */
  const startupState = {authState: 'scrypt', databasePath, mode: 'unlock'};

  const handle = jest.fn(/** @type {IpcMainLike['handle']} */ (() => {
  }));
  const on = jest.fn(/** @type {IpcMainLike['on']} */ (() => {
  }));
  const start = jest.fn(/** @type {(password: string) => Promise<BackendStartOutcome>} */
    (() => Promise.resolve({reachable: true, startedFrom: 'pending'})));
  const verify = jest.fn(/** @type {(databasePath: string, candidate: string) => boolean} */ (() => true));
  const quit = jest.fn();

  /** @type {IpcMainLike} */
  let ipcMain;

  /** @type {Pick<BackendProcess, 'start'>} */
  let backendProcess;

  /** @type {Pick<AuthRegistry, 'verify'>} */
  let authRegistry;

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
    jest.clearAllMocks();
    start.mockResolvedValue({reachable: true, startedFrom: 'pending'});
    verify.mockReturnValue(true);

    ipcMain = {handle, on};
    backendProcess = {start};
    authRegistry = {verify};
    config = {
      env: {FY_DB_FILE_PATH: databasePath},
      auth: {}
    };

    createStartupBridge({ipcMain, startupState, backendProcess, authRegistry, config, quit}).register();
  });

  it('registers exactly the startup:getState, backend:start and auth:verify channels via handle', () => {
    expect(handle.mock.calls.map(([channel]) => channel)).toEqual(['startup:getState', 'backend:start', 'auth:verify']);
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

  // `auth:verify`'s schema is required where `backend:start`'s is optional: a missing argument is rejected here
  // rather than standing in for the empty password
  it('rejects a missing password for auth:verify without reaching authRegistry.verify', () => {
    expect(() => handleListenerFor('auth:verify')(undefined)).toThrow('Invalid password argument for auth:verify');
    expect(verify).not.toHaveBeenCalled();
  });

  it('calls the injected quit for app:quit and spawns no backend and verifies no password', () => {
    onListenerFor('app:quit')(undefined);

    expect(quit).toHaveBeenCalledTimes(1);
    expect(start).not.toHaveBeenCalled();
    expect(verify).not.toHaveBeenCalled();
  });
});
