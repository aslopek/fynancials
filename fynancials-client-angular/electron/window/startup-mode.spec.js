const {beforeEach, describe, expect, it, jest} = require('@jest/globals');
const {createStartupMode} = require('./startup-mode.js');

/** @import {AuthRegistry} from '../config/auth-registry.js' */
/** @import {AuthState} from '../config/auth.js' */
/** @import {ConfigFile, ConfigFileState} from '../config/config-file.js' */
/** @import {FynancialsConfig} from '../config/config-schema.js' */
/** @import {StartupModeResolver} from './startup-mode.js' */

describe('startupMode', () => {
  const databasePath = 'C:\\Users\\x\\fynancials';

  /** @type {FynancialsConfig} */
  let config;

  /** @type {ConfigFileState} */
  let configFileState;

  const save = jest.fn(/** @type {(config: FynancialsConfig) => void} */ (() => undefined));
  const stateOf = jest.fn(/** @type {(databasePath: string) => AuthState} */ (() => 'pending'));

  /** @type {StartupModeResolver} */
  let startupMode;

  /** @returns {StartupModeResolver} */
  function createResolver() {
    /** @type {Pick<ConfigFile, 'save'>} */
    const configFile = {save};

    /** @type {Pick<AuthRegistry, 'stateOf'>} */
    const authRegistry = {stateOf};

    return createStartupMode({configFile, configFileState, config, authRegistry});
  }

  beforeEach(() => {
    jest.clearAllMocks();
    stateOf.mockReturnValue('pending');
    configFileState = 'read';

    config = {
      env: {FY_DB_FILE_PATH: databasePath},
      auth: {}
    };

    startupMode = createResolver();
  });

  it('resolves unlock for a pending database', () => {
    const state = startupMode.resolve();

    expect(state).toEqual({authState: 'pending', databasePath, mode: 'unlock'});
    expect(save).not.toHaveBeenCalled();
  });

  describe('with configureOnNextStart set', () => {
    beforeEach(() => {
      config.configureOnNextStart = true;
    });

    it('resolves configure mode and consumes the flag', () => {
      const state = startupMode.resolve();

      expect(state).toEqual({authState: 'pending', databasePath, mode: 'configure'});
      expect(save).toHaveBeenCalledTimes(1);
      expect(save).toHaveBeenCalledWith({
        env: {FY_DB_FILE_PATH: databasePath},
        auth: {}
      });
    });
  });

  describe('when the config file is missing', () => {
    beforeEach(() => {
      configFileState = 'missing';
      startupMode = createResolver();
    });

    it('resolves configure mode without saving', () => {
      const state = startupMode.resolve();

      expect(state).toEqual({authState: 'pending', databasePath, mode: 'configure'});
      expect(save).not.toHaveBeenCalled();
    });
  });

  describe('when the config file could not be read', () => {
    beforeEach(() => {
      configFileState = 'unreadable';
      startupMode = createResolver();
    });

    it('resolves configure mode without saving', () => {
      const state = startupMode.resolve();

      expect(state).toEqual({authState: 'pending', databasePath, mode: 'configure'});
      expect(save).not.toHaveBeenCalled();
    });

    it('reports no database for the default configuration it fell back to', () => {
      config.env = {};

      const state = startupMode.resolve();

      expect(state).toEqual({authState: null, databasePath: null, mode: 'configure'});
    });

    it('consumes no flag and writes nothing even with configureOnNextStart set', () => {
      config.configureOnNextStart = true;

      const state = startupMode.resolve();

      expect(state).toEqual({authState: 'pending', databasePath, mode: 'configure'});
      expect(config.configureOnNextStart).toBe(true);
      expect(save).not.toHaveBeenCalled();
    });
  });

  describe('when the config names no database', () => {
    beforeEach(() => {
      config.env = {};
    });

    it('resolves configure mode with a null database path and null auth state', () => {
      const state = startupMode.resolve();

      expect(state).toEqual({authState: null, databasePath: null, mode: 'configure'});
      expect(stateOf).not.toHaveBeenCalled();
    });
  });

  describe('when the database has an scrypt entry', () => {
    beforeEach(() => {
      stateOf.mockReturnValue('scrypt');
    });

    it('resolves unlock mode with authState scrypt', () => {
      const state = startupMode.resolve();

      expect(state).toEqual({authState: 'scrypt', databasePath, mode: 'unlock'});
    });
  });

  describe('when the database is passwordless', () => {
    beforeEach(() => {
      stateOf.mockReturnValue('passwordless');
    });

    it('resolves boot mode without saving', () => {
      const state = startupMode.resolve();

      expect(state).toEqual({authState: 'passwordless', databasePath, mode: 'boot'});
      expect(save).not.toHaveBeenCalled();
    });
  });
});
