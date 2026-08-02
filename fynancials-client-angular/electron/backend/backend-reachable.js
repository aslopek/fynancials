/**
 * Observes, in the main process itself, whether a spawned backend became reachable. A 200 on `/config/pid` is what
 * proves the H2 file was decrypted with the password the backend was started with: Liquibase takes a real JDBC
 * connection during context refresh, which is where H2 validates the file password, so a serving HTTP port cannot
 * happen with a wrong password.
 *
 * The renderer polls the backend as well, to know when to leave the splash screen. This poll is separate on purpose -
 * writing an auth record must not depend on a renderer being there.
 */

/** @type {string} */
const BACKEND_PID_URL = 'http://127.0.0.1:23726/config/pid';

/** @type {number} mirror's the renderer's own polling interval */
const POLL_INTERVAL_MILLISECONDS = 500;

/**
 * @typedef {Object} BackendReachabilityOptions
 * @property {() => Promise<boolean>} fetchPid
 * @property {(milliseconds: number) => Promise<void>} delay
 */

/**
 * Subset of `ChildProcess` properties required by this module.
 *
 * @typedef {Object} SpawnedProcess
 * @property {(event: 'exit' | 'error', listener: () => void) => void} on
 */

/**
 * @typedef {Object} BackendReachability
 * @property {(child: SpawnedProcess) => Promise<boolean>} waitUntilReachable
 */

/**
 * @param {BackendReachabilityOptions} options
 * @returns {BackendReachability}
 */
function createBackendReachability(options) {
  const {fetchPid, delay} = options;

  /**
   * Resolves `true` on the first reachable poll, and `false` as soon as the child ends without ever having been
   * reachable. The first outcome wins: a backend that becomes reachable and dies a moment later still counts as a
   * proven start, because the file *was* decrypted.
   *
   * @param {SpawnedProcess} child
   * @returns {Promise<boolean>}
   */
  function waitUntilReachable(child) {
    return new Promise(resolve => {
      let settled = false;

      /**
       * @param {boolean} reachable
       * @returns {void}
       */
      function settle(reachable) {
        if (settled) {
          return;
        }
        settled = true;
        resolve(reachable);
      }

      // 'exit' is a process that ran and ended; 'error' is one that never started - a failed spawn emits 'error' and
      // never 'exit', so listening for the latter alone would poll a port nothing will ever answer on, forever
      child.on('exit', () => settle(false));
      child.on('error', () => settle(false));

      /**
       * @returns {Promise<void>}
       */
      async function poll() {
        /** @type {boolean} */
        let reachable = false;

        while (!settled) {
          reachable = await fetchPid().catch(() => false);
          if (reachable) {
            settle(true);
            return;
          }
          if (settled) {
            return;
          }
          await delay(POLL_INTERVAL_MILLISECONDS);
        }
      }

      poll().catch(() => settle(false));
    });
  }

  return {waitUntilReachable};
}

module.exports = {
  createBackendReachability,
  BACKEND_PID_URL
};
