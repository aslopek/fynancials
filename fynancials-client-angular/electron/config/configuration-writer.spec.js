const {beforeEach, describe, expect, it, jest} = require('@jest/globals');
const {createConfigurationWriter} = require('./configuration-writer.js');
const {storedScryptEntry} = require('../testing/stored-scrypt-entry.js');

/** @import {AuthRegistry} from './auth-registry.js' */
/** @import {AuthState} from './auth.js' */
/** @import {ConfigFile} from './config-file.js' */
/** @import {ConfigurationWriter} from './configuration-writer.js' */
/** @import {FynancialsConfig} from './config-schema.js' */

describe('configurationWriter', () => {
  const databasePath = 'C:\\Users\\x\\fynancials';
  const otherDatabasePath = 'D:\\backup\\fynancials-test';

  /** @type {FynancialsConfig} */
  let config;

  /** @type {ConfigurationWriter} */
  let writer;

  const save = jest.fn(/** @type {(config: FynancialsConfig) => void} */ (() => undefined));
  const stateOf = jest.fn(/** @type {(databasePath: string) => AuthState} */ (() => 'pending'));

  beforeEach(() => {
    jest.clearAllMocks();
    stateOf.mockImplementation((path) => {
      if (path === databasePath) {
        return 'scrypt';
      }
      return path === otherDatabasePath ? 'passwordless' : 'pending';
    });

    config = {
      env: {FY_DB_FILE_PATH: databasePath},
      auth: {
        [databasePath]: storedScryptEntry(),
        [otherDatabasePath]: {passwordless: true}
      }
    };

    /** @type {Pick<ConfigFile, 'save'>} */
    const configFile = {save};

    /** @type {Pick<AuthRegistry, 'stateOf'>} */
    const authRegistry = {stateOf};

    writer = createConfigurationWriter({configFile, config, authRegistry});
  });

  it('writes the selected database into the environment and persists the config', () => {
    writer.apply({databasePath: otherDatabasePath});

    expect(config.env.FY_DB_FILE_PATH).toBe(otherDatabasePath);
    expect(save).toHaveBeenCalledTimes(1);
    expect(save).toHaveBeenCalledWith(config);
  });

  it('leaves every auth entry as it was', () => {
    writer.apply({databasePath: otherDatabasePath});

    expect(config.auth).toEqual({
      [databasePath]: storedScryptEntry(),
      [otherDatabasePath]: {passwordless: true}
    });
  });

  it('returns the state of the newly selected database', () => {
    expect(writer.apply({databasePath: otherDatabasePath})).toBe('passwordless');
  });

  it('returns pending for a database with no entry', () => {
    expect(writer.apply({databasePath: 'E:\\fresh'})).toBe('pending');
  });

  describe('with a further environment entry', () => {
    beforeEach(() => {
      config.env['FY_SOMETHING_ELSE'] = 'value';
    });

    it('leaves it as it was', () => {
      writer.apply({databasePath: otherDatabasePath});

      expect(config.env).toEqual({
        FY_DB_FILE_PATH: otherDatabasePath,
        FY_SOMETHING_ELSE: 'value'
      });
    });
  });

  describe('with configureOnNextStart still set', () => {
    beforeEach(() => {
      config.configureOnNextStart = true;
    });

    it('never touches it', () => {
      writer.apply({databasePath: otherDatabasePath});

      expect(config.configureOnNextStart).toBe(true);
    });
  });
});
