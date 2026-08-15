const {beforeEach, describe, expect, it, jest} = require('@jest/globals');
const {createStartupMode} = require('./startup-mode.js');

/** @import {AuthRegistry} from '../config/auth-registry.js' */
/** @import {AuthState} from '../config/auth.js' */
/** @import {ConfigFile, ConfigFileState} from '../config/config-file.js' */
/** @import {TraQuityConfig} from '../config/config-schema.js' */
/** @import {StartupModeResolver} from './startup-mode.js' */

describe('startupMode', () => {
  const databasePath = 'C:\\Users\\x\\traquity';
  const javaPath = 'C:\\jdk\\bin\\java.exe';

  /** @type {TraQuityConfig} */
  let config;

  /** @type {ConfigFileState} */
  let configFileState;

  /** @type {boolean} */
  let tlsOverridden;

  const save = jest.fn(/** @type {(config: TraQuityConfig) => void} */ (() => undefined));
  const stateOf = jest.fn(/** @type {(databasePath: string) => AuthState} */ (() => 'pending'));
  const resolveJava = jest.fn(/** @type {() => Promise<string | null>} */ (() => Promise.resolve(javaPath)));

  /** @type {StartupModeResolver} */
  let startupMode;

  /** @returns {StartupModeResolver} */
  function createResolver() {
    /** @type {Pick<ConfigFile, 'save'>} */
    const configFile = {save};

    /** @type {Pick<AuthRegistry, 'stateOf'>} */
    const authRegistry = {stateOf};

    return createStartupMode({configFile, configFileState, config, authRegistry, resolveJava, tlsOverridden});
  }

  beforeEach(() => {
    jest.clearAllMocks();
    stateOf.mockReturnValue('pending');
    resolveJava.mockResolvedValue(javaPath);
    configFileState = 'read';
    tlsOverridden = false;

    config = {
      env: {TQ_DB_FILE_PATH: databasePath},
      auth: {}
    };

    startupMode = createResolver();
  });

  it('resolves unlock for a pending database once Java resolves', async () => {
    const state = await startupMode.resolve();

    expect(state).toEqual({authState: 'pending', databasePath, mode: 'unlock'});
    expect(save).not.toHaveBeenCalled();
  });

  describe('with configureOnNextStart set', () => {
    beforeEach(() => {
      config.configureOnNextStart = true;
    });

    it('resolves configure mode and consumes the flag, without probing Java', async () => {
      const state = await startupMode.resolve();

      expect(state).toEqual({authState: 'pending', databasePath, mode: 'configure'});
      expect(save).toHaveBeenCalledTimes(1);
      expect(save).toHaveBeenCalledWith({
        env: {TQ_DB_FILE_PATH: databasePath},
        auth: {}
      });
      expect(resolveJava).not.toHaveBeenCalled();
    });
  });

  describe('when the config file is missing', () => {
    beforeEach(() => {
      configFileState = 'missing';
      startupMode = createResolver();
    });

    it('resolves configure mode without saving or probing Java', async () => {
      const state = await startupMode.resolve();

      expect(state).toEqual({authState: 'pending', databasePath, mode: 'configure'});
      expect(save).not.toHaveBeenCalled();
      expect(resolveJava).not.toHaveBeenCalled();
    });
  });

  describe('when the config file could not be read', () => {
    beforeEach(() => {
      configFileState = 'unreadable';
      startupMode = createResolver();
    });

    it('resolves configure mode without saving', async () => {
      const state = await startupMode.resolve();

      expect(state).toEqual({authState: 'pending', databasePath, mode: 'configure'});
      expect(save).not.toHaveBeenCalled();
    });

    it('reports no database for the default configuration it fell back to', async () => {
      config.env = {};

      const state = await startupMode.resolve();

      expect(state).toEqual({authState: null, databasePath: null, mode: 'configure'});
    });

    it('consumes no flag and writes nothing even with configureOnNextStart set', async () => {
      config.configureOnNextStart = true;

      const state = await startupMode.resolve();

      expect(state).toEqual({authState: 'pending', databasePath, mode: 'configure'});
      expect(config.configureOnNextStart).toBe(true);
      expect(save).not.toHaveBeenCalled();
    });
  });

  describe('when the config names no database', () => {
    beforeEach(() => {
      config.env = {};
    });

    it('resolves configure mode with a null database path and null auth state, without probing Java', async () => {
      const state = await startupMode.resolve();

      expect(state).toEqual({authState: null, databasePath: null, mode: 'configure'});
      expect(stateOf).not.toHaveBeenCalled();
      expect(resolveJava).not.toHaveBeenCalled();
    });
  });

  describe('when the database has an scrypt entry', () => {
    beforeEach(() => {
      stateOf.mockReturnValue('scrypt');
    });

    it('resolves unlock mode with authState scrypt', async () => {
      const state = await startupMode.resolve();

      expect(state).toEqual({authState: 'scrypt', databasePath, mode: 'unlock'});
    });
  });

  describe('when the database is passwordless', () => {
    beforeEach(() => {
      stateOf.mockReturnValue('passwordless');
    });

    it('resolves boot mode without saving', async () => {
      const state = await startupMode.resolve();

      expect(state).toEqual({authState: 'passwordless', databasePath, mode: 'boot'});
      expect(save).not.toHaveBeenCalled();
    });
  });

  describe('when Java does not resolve', () => {
    beforeEach(() => {
      resolveJava.mockResolvedValue(null);
    });

    it('resolves configure mode instead of unlock for a pending database', async () => {
      const state = await startupMode.resolve();

      expect(state).toEqual({authState: 'pending', databasePath, mode: 'configure'});
    });

    it('resolves configure mode instead of boot for a passwordless database', async () => {
      stateOf.mockReturnValue('passwordless');

      const state = await startupMode.resolve();

      expect(state).toEqual({authState: 'passwordless', databasePath, mode: 'configure'});
    });
  });

  describe('when TLS verification is overridden', () => {
    beforeEach(() => {
      tlsOverridden = true;
      configFileState = 'unreadable';
      config.configureOnNextStart = true;
      startupMode = createResolver();
    });

    it('resolves insecure mode ahead of every other branch, probing and writing nothing', async () => {
      const state = await startupMode.resolve();

      expect(state).toEqual({authState: null, databasePath: null, mode: 'insecure'});
      expect(config.configureOnNextStart).toBe(true); // flag not consumed
      // java not resolved, config not read
      expect(save).not.toHaveBeenCalled();
      expect(resolveJava).not.toHaveBeenCalled();
      expect(stateOf).not.toHaveBeenCalled();
    });
  });
});
