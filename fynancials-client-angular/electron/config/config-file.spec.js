const {beforeEach, describe, expect, it, jest} = require('@jest/globals');
const {createConfigFile} = require('./config-file.js');
const {storedScryptEntry} = require('../testing/stored-scrypt-entry.js');

/** @import {ConfigFile, ConfigFileSystem} from './config-file.js' */
/** @import {FynancialsConfig} from './config-schema.js' */

/**
 * Where a home directory actually is on the platform running the specs. The expected default database path is then a
 * literal, rather than the very `path.join` call the module under test makes - which would assert nothing.
 *
 * @typedef {Object} PlatformPaths
 * @property {string} homeDirectory
 * @property {string} configFilePath
 * @property {string} databasePath
 */

/** @returns {PlatformPaths} */
function platformPaths() {
  if (process.platform === 'win32') {
    return {
      homeDirectory: 'C:\\Users\\x',
      configFilePath: 'C:\\Users\\x\\fynancials.config.json',
      databasePath: 'C:\\Users\\x\\fynancials'
    };
  }
  if (process.platform === 'darwin') {
    return {
      homeDirectory: '/Users/x',
      configFilePath: '/Users/x/fynancials.config.json',
      databasePath: '/Users/x/fynancials'
    };
  }
  return {
    homeDirectory: '/home/x',
    configFilePath: '/home/x/fynancials.config.json',
    databasePath: '/home/x/fynancials'
  };
}

describe('configFile', () => {
  const {homeDirectory, configFilePath, databasePath} = platformPaths();

  /**
   * What the default configuration is, spelled out here rather than obtained from `configFile.defaultConfig()` - the
   * module under test may not be the witness of its own fallback, or a wrong default would satisfy every assertion
   * about falling back to it. Key order matters: it is what `save` serializes.
   *
   * @type {FynancialsConfig}
   */
  const defaultConfiguration = {
    env: {FY_DB_FILE_PATH: databasePath},
    auth: {}
  };

  /** @type {string} */
  let storedContents;

  /** @type {ConfigFile} */
  let configFile;

  const existsSync = jest.fn(() => true);
  const readFileSync = jest.fn(() => storedContents);
  const writeFileSync = jest.fn();
  const error = jest.fn();

  beforeEach(() => {
    jest.clearAllMocks();

    storedContents = JSON.stringify({
      env: {FY_DB_FILE_PATH: databasePath},
      auth: {[databasePath]: storedScryptEntry()}
    });

    /** @type {ConfigFileSystem} */
    const fileSystem = {existsSync, readFileSync, writeFileSync};

    configFile = createConfigFile({
      fileSystem,
      configFilePath,
      homeDirectory,
      logger: {error}
    });
  });

  it('loads an existing configuration file', () => {
    const config = configFile.load();

    expect(config.env.FY_DB_FILE_PATH).toBe(databasePath);
    expect(config.auth[databasePath]).toEqual(storedScryptEntry());
    expect(writeFileSync).not.toHaveBeenCalled();
  });

  it('offers a default configuration pointing at the home directory', () => {
    expect(configFile.defaultConfig()).toEqual(defaultConfiguration);
  });

  it('returns the default configuration without writing it when no file exists', () => {
    existsSync.mockReturnValueOnce(false);

    const config = configFile.load();

    expect(config).toEqual(defaultConfiguration);
    expect(writeFileSync).not.toHaveBeenCalled();
  });

  it('reports that the file exists', () => {
    expect(configFile.exists()).toBe(true);
  });

  it('reports that the file does not exist', () => {
    existsSync.mockReturnValueOnce(false);

    expect(configFile.exists()).toBe(false);
  });

  it('keeps an unknown key', () => {
    storedContents = JSON.stringify({
      env: {FY_DB_FILE_PATH: databasePath},
      auth: {},
      foo: 'bar'
    });

    expect(configFile.load()['foo']).toEqual('bar');
  });

  it('defaults missing auth map to an empty mapping', () => {
    storedContents = JSON.stringify({env: {FY_DB_FILE_PATH: databasePath}});

    expect(configFile.load().auth).toEqual({});
  });

  it('keeps arbitrary environment entries', () => {
    storedContents = JSON.stringify({
      env: {
        FY_DB_FILE_PATH: databasePath,
        FY_SOMETHING_ELSE: 'value'
      },
      auth: {}
    });

    expect(configFile.load().env['FY_SOMETHING_ELSE']).toBe('value');
  });

  it('keeps the other entries when one auth entry is mangled', () => {
    const otherDatabasePath = `${databasePath}-backup`;
    storedContents = JSON.stringify({
      env: {FY_DB_FILE_PATH: databasePath},
      auth: {
        [databasePath]: storedScryptEntry(),
        [otherDatabasePath]: {scrypt: {cost: 16384}}
      }
    });

    const config = configFile.load();

    expect(config.auth[databasePath]).toEqual(storedScryptEntry());
    expect(writeFileSync).not.toHaveBeenCalled();
  });

  describe('with an unusable file', () => {
    it.each([
      ['contents that are not JSON', 'not json at all'],
      ['an array', '[]'],
      ['a number', '42'],
      ['a missing env block', '{"auth": {}}'],
      ['an environment entry that is not a string', '{"env": {"FY_DB_FILE_PATH": 42}, "auth": {}}']
    ])('falls back to the default configuration and overwrites %s', (_description, contents) => {
      storedContents = contents;

      const config = configFile.load();

      expect(config).toEqual(defaultConfiguration);
      expect(writeFileSync).toHaveBeenCalledTimes(1);
      expect(writeFileSync).toHaveBeenCalledWith(configFilePath, JSON.stringify(defaultConfiguration, null, 2), {flag: 'w'});
      expect(error).toHaveBeenCalledTimes(1);
    });
  });

  it('logs a failing write rather than throwing', () => {
    writeFileSync.mockImplementationOnce(() => {
      throw new Error('EACCES');
    });

    expect(() => configFile.save(defaultConfiguration)).not.toThrow();
    expect(error).toHaveBeenCalledTimes(1);
  });

  it('reports a failing read of an existing file without throwing', () => {
    readFileSync.mockImplementationOnce(() => {
      throw new Error('EACCES');
    });

    expect(configFile.load()).toEqual(defaultConfiguration);
    expect(error).toHaveBeenCalledTimes(1);
  });
});
