const {beforeEach, describe, expect, it, jest} = require('@jest/globals');
const {createBackendProcess} = require('./backend-process.js');

/** @import {AuthRegistry} from '../config/auth-registry.js' */
/** @import {AuthState} from '../config/auth.js' */
/** @import {FynancialsConfig} from '../config/config-schema.js' */
/** @import {BackendLogFileSystem, BackendProcess, SpawnedBackendProcess} from './backend-process.js' */
/** @import {BackendReachability} from './backend-reachable.js' */

describe('backendProcess', () => {
  const backendPath = 'C:\\app\\resources\\backend.jar';
  const logPath = 'C:\\app\\fynancials.log';
  const databasePath = 'C:\\Users\\x\\fynancials';
  const password = 'hunter2';
  const java = 'java';

  /** @type {FynancialsConfig} */
  let config;

  /** @type {SpawnedBackendProcess} */
  let child;

  const spawn = jest.fn(/** @type {(command: string, args: string[], options: {env: NodeJS.ProcessEnv}) => SpawnedBackendProcess} */
    (() => child));
  const resolveJava = jest.fn(() => Promise.resolve(java));
  const stateOf = jest.fn(/** @type {(databasePath: string) => AuthState} */ (() => 'pending'));
  const recordProvenStart = jest.fn(/** @type {AuthRegistry['recordProvenStart']} */ (() => undefined));
  const waitUntilReachable = jest.fn(() => Promise.resolve(true));
  const createWriteStream = jest.fn(/** @type {(path: string, options: {flags: string}) => import('node:fs').WriteStream} */
    (() => logStream));
  const error = jest.fn(/** @type {(message: string, cause: unknown) => void} */ (() => undefined));
  // a minimal stand-in for `WriteStream` - only the members this module actually calls on it
  const logStream = /** @type {import('node:fs').WriteStream} */ (/** @type {unknown} */ ({on: jest.fn(), end: jest.fn()}));
  const childOn = jest.fn(/** @type {SpawnedBackendProcess['on']} */ (() => undefined));
  const childStdoutPipe = jest.fn(/** @type {SpawnedBackendProcess['stdout']['pipe']} */ (() => undefined));
  const childStderrPipe = jest.fn(/** @type {SpawnedBackendProcess['stderr']['pipe']} */ (() => undefined));
  const childKill = jest.fn(/** @type {SpawnedBackendProcess['kill']} */ (() => undefined));

  /** @type {BackendProcess} */
  let backendProcess;

  beforeEach(() => {
    jest.clearAllMocks();
    resolveJava.mockResolvedValue(java);
    stateOf.mockReturnValue('pending');
    waitUntilReachable.mockResolvedValue(true);
    createWriteStream.mockReturnValue(logStream);

    config = {
      env: {FY_DB_FILE_PATH: databasePath},
      auth: {}
    };

    child = {
      on: childOn,
      stdout: {pipe: childStdoutPipe},
      stderr: {pipe: childStderrPipe},
      kill: childKill
    };
    spawn.mockImplementation(() => child);

    /** @type {Pick<AuthRegistry, 'recordProvenStart' | 'stateOf'>} */
    const authRegistry = {recordProvenStart, stateOf};

    /** @type {BackendReachability} */
    const backendReachability = {waitUntilReachable};

    /** @type {BackendLogFileSystem} */
    const logFileSystem = {createWriteStream};

    backendProcess = createBackendProcess({
      spawn,
      resolveJava,
      backendPath,
      config,
      authRegistry,
      backendReachability,
      logFileSystem,
      logPath,
      logger: {error}
    });
  });

  it('spawns the resolved java binary with the jar and the given password', async () => {
    await backendProcess.start(password);

    expect(spawn).toHaveBeenCalledTimes(1);
    expect(spawn).toHaveBeenCalledWith(java, ['-jar', backendPath], {
      env: expect.objectContaining({
        FY_DB_FILE_PATH: databasePath,
        FY_DB_FILE_PASSWORD: password
      })
    });
  });

  it('resolves with a reachable outcome and the state it was started from', async () => {
    stateOf.mockReturnValue('scrypt');

    const outcome = await backendProcess.start(password);

    expect(outcome).toEqual({reachable: true, startedFrom: 'scrypt'});
  });

  it('resolves with an unreachable outcome', async () => {
    waitUntilReachable.mockResolvedValue(false);

    const outcome = await backendProcess.start(password);

    expect(outcome).toEqual({reachable: false, startedFrom: 'pending'});
  });

  it('records a proven start when reachable', async () => {
    await backendProcess.start(password);

    expect(recordProvenStart).toHaveBeenCalledTimes(1);
    expect(recordProvenStart).toHaveBeenCalledWith(databasePath, password);
  });

  it('does not record a proven start when unreachable', async () => {
    waitUntilReachable.mockResolvedValue(false);

    await backendProcess.start(password);

    expect(recordProvenStart).not.toHaveBeenCalled();
  });

  it('does not record a proven start when no database is configured', async () => {
    config.env = {};

    await backendProcess.start(password);

    expect(recordProvenStart).not.toHaveBeenCalled();
  });

  it('pipes stdout and stderr into the log file in append mode', async () => {
    await backendProcess.start(password);

    expect(createWriteStream).toHaveBeenCalledTimes(1);
    expect(createWriteStream).toHaveBeenCalledWith(logPath, {flags: 'a'});

    expect(childStdoutPipe).toHaveBeenCalledTimes(1);
    expect(childStdoutPipe).toHaveBeenCalledWith(logStream, {end: false});

    expect(childStderrPipe).toHaveBeenCalledTimes(1);
    expect(childStderrPipe).toHaveBeenCalledWith(logStream, {end: false});
  });

  describe('while a backend is running', () => {
    beforeEach(async () => {
      await backendProcess.start(password);
      jest.clearAllMocks();
    });

    it('rejects a second start', async () => {
      await expect(backendProcess.start(password)).rejects.toThrow('A backend is already running');
    });

    it('does not spawn a second child process', async () => {
      await backendProcess.start(password).catch(() => undefined);

      expect(spawn).not.toHaveBeenCalled();
    });
  });

  describe('while a start is still resolving java', () => {
    beforeEach(() => {
      // a java resolution that never settles keeps the first start in flight for the whole test, which is the
      // window in which nothing has been spawned yet and `child` is still null
      resolveJava.mockImplementation(() => new Promise(() => undefined));
      void backendProcess.start(password);
    });

    it('rejects a second start', async () => {
      await expect(backendProcess.start(password)).rejects.toThrow('A backend is already running');
    });

    it('does not spawn a child process for the second start', async () => {
      await backendProcess.start(password).catch(() => undefined);

      expect(spawn).not.toHaveBeenCalled();
    });
  });

  describe('after the child exited', () => {
    beforeEach(async () => {
      await backendProcess.start(password);
      const exitCall = childOn.mock.calls.find(([event]) => event === 'exit');
      const exitListener = /** @type {() => void} */ (exitCall?.[1]);
      exitListener();
    });

    it('allows a retry that spawns again with the given password', async () => {
      // clears the first start's spawn call, so the assertion below can only be satisfied by the retry's own
      jest.clearAllMocks();
      const retryPassword = 'hunter3';

      await backendProcess.start(retryPassword);

      expect(spawn).toHaveBeenCalledWith(java, ['-jar', backendPath], {
        env: expect.objectContaining({FY_DB_FILE_PASSWORD: retryPassword})
      });
    });
  });

  describe('kill', () => {
    it('does nothing when no backend has been started', () => {
      expect(() => backendProcess.kill()).not.toThrow();
      expect(childKill).not.toHaveBeenCalled();
    });

    it('sends SIGTERM to the running child', async () => {
      await backendProcess.start(password);

      backendProcess.kill();

      expect(childKill).toHaveBeenCalledWith('SIGTERM');
    });

    it('forgets the child, so a later start spawns again', async () => {
      await backendProcess.start(password);
      backendProcess.kill();
      // clears the first start's spawn call, so the assertion below can only be satisfied by the second one
      jest.clearAllMocks();

      await backendProcess.start(password);

      expect(spawn).toHaveBeenCalledTimes(1);
    });
  });
});
