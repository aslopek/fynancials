const {beforeEach, describe, expect, it, jest} = require('@jest/globals');
const {createBackendReachability} = require('./backend-reachable.js');

/** @import {BackendReachability, SpawnedProcess} from './backend-reachable.js' */

describe('backendReachability', () => {
  const fetchPid = jest.fn(async () => true);
  const delay = jest.fn(async () => {
  });

  /** @type {Map<'exit' | 'error', () => void>} */
  let listeners;

  /** @type {SpawnedProcess} */
  let child;

  /** @type {BackendReachability} */
  let reachability;

  beforeEach(() => {
    jest.clearAllMocks();
    fetchPid.mockResolvedValue(true);

    listeners = new Map();
    child = {on: registerListener};

    reachability = createBackendReachability({fetchPid, delay});
  });

  it('reports a backend that answers on the first poll as reachable', async () => {
    await expect(reachability.waitUntilReachable(child)).resolves.toBe(true);

    expect(fetchPid).toHaveBeenCalledTimes(1);
    expect(delay).not.toHaveBeenCalled();
  });

  it('keeps polling until the backend answers', async () => {
    fetchPid.mockResolvedValueOnce(false);

    await expect(reachability.waitUntilReachable(child)).resolves.toBe(true);

    expect(fetchPid).toHaveBeenCalledTimes(2);
    expect(delay).toHaveBeenCalledTimes(1);
  });

  it('treats a failing poll as not reachable rather than as an error', async () => {
    fetchPid.mockRejectedValueOnce(new Error('connect ECONNREFUSED 127.0.0.1:23726'));

    await expect(reachability.waitUntilReachable(child)).resolves.toBe(true);

    expect(fetchPid).toHaveBeenCalledTimes(2);
    expect(delay).toHaveBeenCalledTimes(1);
  });

  describe('with a backend that never answers', () => {
    beforeEach(() => {
      fetchPid.mockResolvedValue(false);
    });

    it('reports a child that exits as not reachable and stops polling', async () => {
      const reachable = reachability.waitUntilReachable(child);
      emit('exit');

      await expect(reachable).resolves.toBe(false);

      await flushPendingWork();
      expect(fetchPid).toHaveBeenCalledTimes(1);
      expect(delay).toHaveBeenCalledTimes(0);
    });

    it('reports a child that never spawned as not reachable and stops polling', async () => {
      const reachable = reachability.waitUntilReachable(child);
      emit('error');

      await expect(reachable).resolves.toBe(false);

      await flushPendingWork();
      expect(fetchPid).toHaveBeenCalledTimes(1);
      expect(delay).toHaveBeenCalledTimes(0);
    });

    it('keeps waiting while the child is alive', async () => {
      // a poll interval that has not elapsed yet
      delay.mockReturnValue(new Promise(() => {
      }));

      /** @type {string | boolean} */
      let outcome = 'still waiting';
      void reachability.waitUntilReachable(child).then(reachable => outcome = reachable);

      await flushPendingWork();

      expect(outcome).toBe('still waiting');
      expect(delay).toHaveBeenCalledTimes(1);
    });
  });

  /**
   * Stands in for the child process' own listener registration, so that {@link emit} can reach the listeners the module
   * registers internally.
   *
   * @param {'exit' | 'error'} event
   * @param {() => void} listener
   * @returns {void}
   */
  function registerListener(event, listener) {
    listeners.set(event, listener);
  }

  /**
   * Invokes the listener the module registered for `event`, standing in for the operating system emitting it on a real
   * child process. Throws rather than doing nothing when there is no listener, so a module that stopped registering one
   * fails here instead of hanging until the test times out.
   *
   * @param {'exit' | 'error'} event
   * @returns {void}
   */
  function emit(event) {
    const listener = listeners.get(event);
    if (!listener) {
      throw new Error(`the module registered no '${event}' listener`);
    }
    listener();
  }

  /**
   * Resolves after everything the polling loop has queued so far has run, so an assertion about calls that must *not*
   * happen cannot pass merely because the loop had no chance to make them yet.
   *
   * @returns {Promise<void>}
   */
  function flushPendingWork() {
    return new Promise(resolve => {
      setImmediate(resolve);
    });
  }
});
