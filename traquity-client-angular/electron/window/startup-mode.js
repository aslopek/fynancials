/** @import {AuthRegistry} from '../config/auth-registry.js' */
/** @import {AuthState} from '../config/auth.js' */
/** @import {ConfigFile, ConfigFileState} from '../config/config-file.js' */
/** @import {TraQuityConfig} from '../config/config-schema.js' */

/**
 * Computes which startup mode the window opens into, from the loaded config, the auth registry and a resolved Java
 * runtime. Consuming the one-shot `configureOnNextStart` flag lives here too: it is read and deleted from the config
 * in the same step that decides the mode, so the flag cannot outlive the run it was written for.
 *
 * `resolve()` is a one-shot snapshot rather than a live view - it consumes that flag on the way, so a second call is
 * not equivalent to the first. What it reports describes the database the app started against, and stays true for
 * the whole run on its own terms: a failed start leaves the config untouched, and a proven start only ever fills a
 * pending entry.
 *
 * The Java probe runs last, after every branch that already ends in `configure` mode without it - those spawn no JVM
 * at all - and its failure overrides what the auth state alone would have picked, `boot` or `unlock` alike: a
 * database this run cannot reach Java for has no business asking for a password it could never verify against a
 * running backend.
 */

/** @typedef {'boot' | 'configure' | 'insecure' | 'unlock'} StartupMode */

/**
 * @typedef {Object} StartupState
 * @property {AuthState | null} authState null when the config names no database
 * @property {string | null} databasePath database base path without extension, null when the config names none
 * @property {StartupMode} mode
 */

/**
 * @typedef {Object} StartupModeOptions
 * @property {Pick<ConfigFile, 'save'>} configFile
 * @property {ConfigFileState} configFileState what the single `load()` at start observed about the file
 * @property {TraQuityConfig} config
 * @property {Pick<AuthRegistry, 'stateOf'>} authRegistry
 * @property {() => Promise<string | null>} resolveJava
 * @property {boolean} tlsOverridden
 */

/** @typedef {{resolve: () => Promise<StartupState>}} StartupModeResolver */

/**
 * @param {StartupModeOptions} options
 * @returns {StartupModeResolver}
 */
function createStartupMode(options) {
  const {configFile, configFileState, config, authRegistry, resolveJava, tlsOverridden} = options;

  /**
   * @returns {Promise<StartupState>}
   */
  async function resolve() {
    if (tlsOverridden) {
      return {authState: null, databasePath: null, mode: 'insecure'};
    }

    /** @type {string | null} */
    const databasePath = config.env.TQ_DB_FILE_PATH ?? null;
    /** @type {AuthState | null} */
    const authState = databasePath == null ? null : authRegistry.stateOf(databasePath);

    // first, so that no ordering of later edits can make a config file we failed to read get written to. The two
    // branches can never both apply - a file that could not be read yields the default, which carries no flag -
    // so the order costs nothing and makes the one branch below that writes unreachable for such a file.
    if (configFileState !== 'read') {
      return {authState, databasePath, mode: 'configure'};
    }

    if (config.configureOnNextStart === true) {
      delete config.configureOnNextStart;
      configFile.save(config);
      return {authState, databasePath, mode: 'configure'};
    }

    if (databasePath == null) {
      return {authState, databasePath, mode: 'configure'};
    }

    const java = await resolveJava();
    if (java == null) {
      return {authState, databasePath, mode: 'configure'};
    }

    return {authState, databasePath, mode: authState === 'passwordless' ? 'boot' : 'unlock'};
  }

  return {resolve};
}

module.exports = {createStartupMode};
