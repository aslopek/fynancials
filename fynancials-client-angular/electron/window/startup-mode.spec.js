const {beforeEach, describe, expect, it, jest} = require('@jest/globals');
const {createStartupMode} = require('./startup-mode.js');

/** @import {AuthRegistry} from '../config/auth-registry.js' */
/** @import {AuthState} from '../config/auth.js' */
/** @import {ConfigFile} from '../config/config-file.js' */
/** @import {FynancialsConfig} from '../config/config-schema.js' */
/** @import {StartupModeResolver} from './startup-mode.js' */

describe('startupMode', () => {
  const databasePath = 'C:\\Users\\x\\fynancials';

  /** @type {FynancialsConfig} */
  let config;

  const exists = jest.fn(() => true);
  const save = jest.fn(/** @type {(config: FynancialsConfig) => void} */ (() => undefined));
  const stateOf = jest.fn(/** @type {(databasePath: string) => AuthState} */ (() => 'pending'));

  /** @type {StartupModeResolver} */
  let startupMode;

  beforeEach(() => {
    jest.clearAllMocks();
    exists.mockReturnValue(true);
    stateOf.mockReturnValue('pending');

    config = {
      env: {FY_DB_FILE_PATH: databasePath},
      auth: {}
    };

    /** @type {Pick<ConfigFile, 'exists' | 'save'>} */
    const configFile = {exists, save};

    /** @type {Pick<AuthRegistry, 'stateOf'>} */
    const authRegistry = {stateOf};

    startupMode = createStartupMode({configFile, config, authRegistry});
  });

  it('resolves unlock for a pending database', () => {
    const state = startupMode.resolve();

    expect(state).toEqual({databasePath, mode: 'unlock'});
    expect(save).not.toHaveBeenCalled();
  });

  describe('with configureOnNextStart set', () => {
    beforeEach(() => {
      config.configureOnNextStart = true;
    });

    it('resolves configure mode and consumes the flag', () => {
      const state = startupMode.resolve();

      expect(state).toEqual({databasePath, mode: 'configure'});
      expect(save).toHaveBeenCalledTimes(1);
      expect(save).toHaveBeenCalledWith({
        env: {FY_DB_FILE_PATH: databasePath},
        auth: {}
      });
    });
  });

  describe('when the config file does not exist', () => {
    beforeEach(() => {
      exists.mockReturnValue(false);
    });

    it('resolves configure mode without saving', () => {
      const state = startupMode.resolve();

      expect(state).toEqual({databasePath, mode: 'configure'});
      expect(save).not.toHaveBeenCalled();
    });
  });

  describe('when the config names no database', () => {
    beforeEach(() => {
      config.env = {};
    });

    it('resolves configure mode with a null database path', () => {
      const state = startupMode.resolve();

      expect(state).toEqual({databasePath: null, mode: 'configure'});
    });
  });

  describe('when the database has an scrypt entry', () => {
    beforeEach(() => {
      stateOf.mockReturnValue('scrypt');
    });

    it('resolves unlock mode', () => {
      const state = startupMode.resolve();

      expect(state).toEqual({databasePath, mode: 'unlock'});
    });
  });

  describe('when the database is passwordless', () => {
    beforeEach(() => {
      stateOf.mockReturnValue('passwordless');
    });

    it('resolves boot mode', () => {
      const state = startupMode.resolve();

      expect(state).toEqual({databasePath, mode: 'boot'});
    });
  });
});
