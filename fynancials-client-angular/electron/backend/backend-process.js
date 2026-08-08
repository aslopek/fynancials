/** @import {AuthRegistry} from '../config/auth-registry.js' */
/** @import {AuthState} from '../config/auth.js' */
/** @import {FynancialsConfig} from '../config/config-schema.js' */
/** @import {BackendReachability} from './backend-reachable.js' */

/**
 * Owns the backend child process: spawning it, piping its log, guarding against a second concurrent spawn, and
 * recording a proven start. The config is read live at spawn time (epic story #37 mutates the loaded config object
 * before triggering a start; a value captured at construction would silently spawn against an old database).
 *
 * Never logs the password, not even in an error path - `config/auth.js`'s rule applies here too.
 */

/**
 * What a start attempt says about the database it was made against. `startedFrom` is the state read *before* the
 * password was applied, which is what a failed start has to be routed on (epic ADR-003).
 *
 * @typedef {{reachable: boolean, startedFrom: AuthState}} BackendStartOutcome
 */

/**
 * The subset of `fs` required to log the backend's output - declared minimally, according to the
 * architecture guidelines.
 *
 * @typedef {Object} BackendLogFileSystem
 * @property {(path: string, options: {flags: string}) => import('node:fs').WriteStream} createWriteStream
 */

/**
 * The subset of `ChildProcessWithoutNullStreams` this module actually touches - declared minimally,
 * according to the architecture guidelines.
 *
 * @typedef {Object} SpawnedBackendProcess
 * @property {(event: 'exit' | 'error' | 'close', listener: () => void) => void} on
 * @property {{pipe: (destination: import('node:fs').WriteStream, options: {end: boolean}) => void}} stdout
 * @property {{pipe: (destination: import('node:fs').WriteStream, options: {end: boolean}) => void}} stderr
 * @property {(signal: NodeJS.Signals) => void} kill
 */

/**
 * @typedef {Object} BackendProcessOptions
 * @property {(command: string, args: string[], options: {env: NodeJS.ProcessEnv}) => SpawnedBackendProcess} spawn
 * @property {() => Promise<string>} resolveJava injected so `verifyJava` stays in main.js and #38 can replace it
 * @property {string} backendPath
 * @property {FynancialsConfig} config read live at spawn time
 * @property {Pick<AuthRegistry, 'recordProvenStart' | 'stateOf'>} authRegistry
 * @property {BackendReachability} backendReachability
 * @property {BackendLogFileSystem} logFileSystem
 * @property {string} logPath
 * @property {Pick<Console, 'error'>} [logger]
 */

/**
 * @typedef {Object} BackendProcess
 * @property {(password: string) => Promise<BackendStartOutcome>} start
 * @property {() => void} kill
 */

/**
 * @param {BackendProcessOptions} options
 * @returns {BackendProcess}
 */
function createBackendProcess(options) {
  const {spawn, resolveJava, backendPath, config, authRegistry, backendReachability, logFileSystem, logPath} = options;
  const logger = options.logger ?? console;

  /** @type {SpawnedBackendProcess | null} */
  let child = null;

  // set synchronously by `start`, before its first `await`. Java resolution is itself a spawned process, so a guard
  // reading `child` alone would let two overlapping calls through and spawn twice - and the first child would then be
  // orphaned, with nothing holding a reference `kill()` could reach.
  let starting = false;

  /**
   * Whether a backend is running or on its way up - both refuse a further start.
   *
   * @returns {boolean}
   */
  function isRunning() {
    return starting || child != null;
  }

  /**
   * @param {string} password
   * @returns {Promise<BackendStartOutcome>}
   */
  async function start(password) {
    if (isRunning()) {
      throw new Error('A backend is already running');
    }
    starting = true;

    try {
      /** @type {string | null} */
      const databasePath = config.env.FY_DB_FILE_PATH ?? null;
      /** @type {AuthState} */
      const startedFrom = databasePath == null ? 'pending' : authRegistry.stateOf(databasePath);

      const java = await resolveJava();
      const spawnedProcess = spawn(java, ['-jar', backendPath], {
        env: {
          ...process.env,
          ...config.env,
          FY_DB_FILE_PASSWORD: password
        }
      });
      child = spawnedProcess;

      /**
       * @returns {void}
       */
      function forget() {
        if (child === spawnedProcess) {
          child = null;
        }
      }

      spawnedProcess.on('exit', forget);
      spawnedProcess.on('error', forget);
      pipeLog(spawnedProcess);

      // a backend that answers proves the H2 file was decrypted with this password - and only then is it recorded
      const reachable = await backendReachability.waitUntilReachable(spawnedProcess);
      if (reachable && databasePath != null) {
        authRegistry.recordProvenStart(databasePath, password);
      }
      return {reachable, startedFrom};
    } finally {
      // a start that got as far as a running child leaves `child` to answer `isRunning`; a failed one has already
      // cleared it from the child's own `exit`/`error`, which is what lets the renderer retry
      starting = false;
    }
  }

  /**
   * @param {SpawnedBackendProcess} spawnedProcess
   * @returns {void}
   */
  function pipeLog(spawnedProcess) {
    try {
      /** @type {import('node:fs').WriteStream} */
      const logStream = logFileSystem.createWriteStream(logPath, {flags: 'a'});
      logStream.on('error', (error) => logger.error(`Failed to write backend log to ${logPath}:`, error));
      spawnedProcess.stdout.pipe(logStream, {end: false});
      spawnedProcess.stderr.pipe(logStream, {end: false});
      spawnedProcess.on('close', () => logStream.end());
    } catch (error) {
      logger.error(`Failed to set up backend logging at ${logPath}:`, error);
    }
  }

  /**
   * @returns {void}
   */
  function kill() {
    if (child != null) {
      child.kill('SIGTERM');
      child = null;
    }
  }

  return {start, kill};
}

module.exports = {createBackendProcess};
