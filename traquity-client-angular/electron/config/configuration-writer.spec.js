const {beforeEach, describe, expect, it, jest} = require('@jest/globals');
const {createConfigurationWriter} = require('./configuration-writer.js');
const {storedScryptEntry} = require('../testing/stored-scrypt-entry.js');

/** @import {AuthRegistry} from './auth-registry.js' */
/** @import {AuthState} from './auth.js' */
/** @import {ConfigFile} from './config-file.js' */
/** @import {ConfigurationWriter} from './configuration-writer.js' */
/** @import {TraQuityConfig} from './config-schema.js' */

describe('configurationWriter', () => {
  const databasePath = 'C:\\Users\\x\\traquity';
  const otherDatabasePath = 'D:\\backup\\traquity-test';

  /** @type {TraQuityConfig} */
  let config;

  /** @type {ConfigurationWriter} */
  let writer;

  const save = jest.fn(/** @type {(config: TraQuityConfig) => void} */ (() => undefined));
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
      env: {TQ_DB_FILE_PATH: databasePath},
      auth: {
        [databasePath]: storedScryptEntry(),
        [otherDatabasePath]: {passwordless: true}
      },
      java: {path: null}
    };

    /** @type {Pick<ConfigFile, 'save'>} */
    const configFile = {save};

    /** @type {Pick<AuthRegistry, 'stateOf'>} */
    const authRegistry = {stateOf};

    writer = createConfigurationWriter({configFile, config, authRegistry});
  });

  it('writes the selected database into the environment and persists the config', () => {
    writer.apply({databasePath: otherDatabasePath, javaPath: null, javaSignature: null});

    expect(config.env.TQ_DB_FILE_PATH).toBe(otherDatabasePath);
    expect(save).toHaveBeenCalledTimes(1);
    expect(save).toHaveBeenCalledWith(config);
  });

  it('leaves every auth entry as it was', () => {
    writer.apply({databasePath: otherDatabasePath, javaPath: null, javaSignature: null});

    expect(config.auth).toEqual({
      [databasePath]: storedScryptEntry(),
      [otherDatabasePath]: {passwordless: true}
    });
  });

  it('returns the state of the newly selected database', () => {
    expect(writer.apply({databasePath: otherDatabasePath, javaPath: null, javaSignature: null})).toBe('passwordless');
  });

  it('returns pending for a database with no entry', () => {
    expect(writer.apply({databasePath: 'E:\\fresh', javaPath: null, javaSignature: null})).toBe('pending');
  });

  it('writes a custom java path with no signature', () => {
    writer.apply({databasePath: otherDatabasePath, javaPath: 'C:\\jdk\\bin\\java.exe', javaSignature: null});

    expect(config.java).toEqual({path: 'C:\\jdk\\bin\\java.exe', signature: null});
  });

  it('writes a downloaded java path together with its signature', () => {
    writer.apply({databasePath: otherDatabasePath, javaPath: 'C:\\java\\bin\\java.exe', javaSignature: 'c2ln'});

    expect(config.java).toEqual({path: 'C:\\java\\bin\\java.exe', signature: 'c2ln'});
  });

  it('writes null as null for automatic resolution, not omitted', () => {
    config.java = {path: 'C:\\jdk\\bin\\java.exe', signature: 'c2ln'};

    writer.apply({databasePath: otherDatabasePath, javaPath: null, javaSignature: null});

    expect(config.java).toEqual({path: null, signature: null});
  });

  it('keeps unknown sub-keys of java', () => {
    config.java = {path: null, extra: 'kept'};

    writer.apply({databasePath: otherDatabasePath, javaPath: 'C:\\jdk\\bin\\java.exe', javaSignature: null});

    expect(config.java).toEqual({path: 'C:\\jdk\\bin\\java.exe', signature: null, extra: 'kept'});
  });

  describe('with a further environment entry', () => {
    beforeEach(() => {
      config.env['TQ_SOMETHING_ELSE'] = 'value';
    });

    it('leaves it as it was', () => {
      writer.apply({databasePath: otherDatabasePath, javaPath: null, javaSignature: null});

      expect(config.env).toEqual({
        TQ_DB_FILE_PATH: otherDatabasePath,
        TQ_SOMETHING_ELSE: 'value'
      });
    });
  });

  describe('with configureOnNextStart still set', () => {
    beforeEach(() => {
      config.configureOnNextStart = true;
    });

    it('never touches it', () => {
      writer.apply({databasePath: otherDatabasePath, javaPath: null, javaSignature: null});

      expect(config.configureOnNextStart).toBe(true);
    });
  });
});
