/**
 * The environment a JVM this app spawns is allowed to see. A JVM reads command-line arguments out of its own
 * environment, so an inherited variable is an inherited argument: `JAVA_TOOL_OPTIONS`, `JDK_JAVA_OPTIONS` and
 * `_JAVA_OPTIONS` are each appended to what the process was invoked with, which turns any of them into a way to load
 * an agent into a JVM that was asked to do something else entirely. `FY_DB_FILE_PASSWORD` goes for a different
 * reason: the database password reaches a backend over its stdin, and an environment block is readable to other
 * processes on every platform this app ships to.
 *
 * Removing them here rather than at each spawn site is what makes the rule one rule: a JVM spawned anywhere in this
 * app passes through this function, and a variable added to the list below is stripped from all of them at once.
 */

/**
 * @type {string[]}
 */
const STRIPPED_KEYS = ['FY_DB_FILE_PASSWORD', 'JAVA_TOOL_OPTIONS', 'JDK_JAVA_OPTIONS', '_JAVA_OPTIONS'];

/**
 * A copy of `environment` without the entries above - the input is never mutated, so a caller may pass
 * `process.env` itself.
 *
 * @param {NodeJS.ProcessEnv} environment
 * @returns {NodeJS.ProcessEnv}
 */
function jvmEnvironment(environment) {
  /** @type {NodeJS.ProcessEnv} */
  const sanitized = {...environment};
  for (const key of STRIPPED_KEYS) {
    delete sanitized[key];
  }
  return sanitized;
}

module.exports = {jvmEnvironment, STRIPPED_KEYS};
