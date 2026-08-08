const {beforeEach, describe, expect, it, jest} = require('@jest/globals');
const {createStartupBridge} = require('./startup-bridge.js');

/** @import {BackendProcess, BackendStartOutcome} from '../backend/backend-process.js' */
/** @import {StartupState} from '../window/startup-mode.js' */
/** @import {IpcMainLike} from './startup-bridge.js' */

describe('startupBridge', () => {
  /** @type {StartupState} */
  const startupState = {databasePath: 'C:\\Users\\x\\fynancials', mode: 'unlock'};

  const handle = jest.fn(/** @type {IpcMainLike['handle']} */ (() => {
  }));
  const start = jest.fn(/** @type {(password: string) => Promise<BackendStartOutcome>} */
    (() => Promise.resolve({reachable: true, startedFrom: 'pending'})));
  /** @type {IpcMainLike} */
  let ipcMain;

  /** @type {Pick<BackendProcess, 'start'>} */
  let backendProcess;

  /**
   * @param {string} channel
   * @returns {(event: unknown, ...args: unknown[]) => unknown}
   */
  function listenerFor(channel) {
    const call = handle.mock.calls.find(([registeredChannel]) => registeredChannel === channel);
    if (call == null) {
      throw new Error(`No handler registered for ${channel}`);
    }
    return call[1];
  }

  beforeEach(() => {
    jest.clearAllMocks();
    start.mockResolvedValue({reachable: true, startedFrom: 'pending'});

    ipcMain = {handle};
    backendProcess = {start};

    createStartupBridge({ipcMain, startupState, backendProcess}).register();
  });

  it('registers exactly the startup:getState and backend:start channels', () => {
    expect(handle.mock.calls.map(([channel]) => channel)).toEqual(['startup:getState', 'backend:start']);
  });

  it('resolves startup:getState with the given startup state', () => {
    expect(listenerFor('startup:getState')(undefined)).toBe(startupState);
  });

  it('delegates a string password to backendProcess.start', async () => {
    await listenerFor('backend:start')(undefined, 'hunter2');

    expect(start).toHaveBeenCalledWith('hunter2');
  });

  it('delegates an empty password when none is given', async () => {
    await listenerFor('backend:start')(undefined);

    expect(start).toHaveBeenCalledWith('');
  });

  it('rejects a non-string password without reaching backendProcess.start', () => {
    expect(() => listenerFor('backend:start')(undefined, 42)).toThrow('Invalid password argument for backend:start');
    expect(start).not.toHaveBeenCalled();
  });

  it('propagates a rejection from backendProcess.start', async () => {
    start.mockRejectedValue(new Error('A backend is already running'));

    await expect(listenerFor('backend:start')(undefined, 'hunter2')).rejects.toThrow('A backend is already running');
  });
});
