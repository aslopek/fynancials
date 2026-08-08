const {beforeEach, describe, expect, it, jest} = require('@jest/globals');
const {createAuthRegistry} = require('./auth-registry.js');
const {verifyPassword} = require('./auth.js');
const {base64Of} = require('../testing/base64-of.js');
const {STORED_SCRYPT_PASSWORD, storedScryptEntry} = require('../testing/stored-scrypt-entry.js');

/** @import {AuthRegistry} from './auth-registry.js' */
/** @import {ConfigFile} from './config-file.js' */
/** @import {AuthEntry, FynancialsConfig} from './config-schema.js' */

/**
 * What `recordProvenStart` did is read off the config object it was handed - never off `stateOf`/`verify`, which are
 * under test here themselves and would make the registry the judge of its own writes.
 */
describe('authRegistry', () => {
  const databasePath = 'C:\\Users\\x\\fynancials';
  const password = 'hunter2';

  // Mirrors auth.js's own SALT_BYTES/HASH_BYTES - what a proven start must actually produce.
  const SALT_BYTES = 16;
  const HASH_BYTES = 32;

  /** @type {FynancialsConfig} */
  let config;

  /** @type {Pick<ConfigFile, 'save'>} */
  let configFile;

  /** @type {AuthRegistry} */
  let registry;

  const save = jest.fn();

  beforeEach(() => {
    jest.clearAllMocks();

    config = {
      env: {FY_DB_FILE_PATH: databasePath},
      auth: {}
    };

    configFile = {save};

    registry = createAuthRegistry({configFile, config});
  });

  it('records a scrypt entry for a proven start of a pending database', () => {
    registry.recordProvenStart(databasePath, password);

    expect(config.auth).toEqual({
      [databasePath]: {
        scrypt: {
          salt: base64Of(SALT_BYTES),
          hash: base64Of(HASH_BYTES),
          cost: 16384,
          blockSize: 8,
          parallelization: 1
        }
      }
    });
  });

  // The one claim the written state cannot carry on its own: the salt is random, so the hash of the proven password
  // is not a literal this spec could state. `auth.js` is a collaborator here with a spec of its own - the assertion
  // therefore goes through it rather than through `registry.verify`, which would make the registry judge its own write.
  it('records a scrypt entry the proven password verifies against', () => {
    registry.recordProvenStart(databasePath, password);

    expect(verifyPassword(config.auth[databasePath], password)).toBe(true);
  });

  it('saves the config it wrote the entry into', () => {
    registry.recordProvenStart(databasePath, password);

    expect(save).toHaveBeenCalledTimes(1);
    expect(save).toHaveBeenCalledWith(config);
  });

  it('records the passwordless marker for a proven start with an empty password', () => {
    registry.recordProvenStart(databasePath, '');

    expect(config.auth).toEqual({[databasePath]: {passwordless: true}});
  });

  it('never writes the password into what it persists', () => {
    registry.recordProvenStart(databasePath, password);

    expect(JSON.stringify(config)).not.toContain(password);
  });

  it('reads a database without an entry as pending', () => {
    expect(registry.stateOf(databasePath)).toBe('pending');
  });

  it('verifies nothing against a database without an entry', () => {
    expect(registry.verify(databasePath, password)).toBe(false);
  });

  describe('with an entry for another database', () => {
    const otherDatabasePath = 'D:\\backup\\fynancials-test';

    /** @type {AuthEntry} */
    let otherEntry;

    beforeEach(() => {
      otherEntry = storedScryptEntry();
      config.auth[otherDatabasePath] = otherEntry;
    });

    it('reads the database as pending', () => {
      expect(registry.stateOf(databasePath)).toBe('pending');
    });

    it('leaves that entry untouched on a proven start', () => {
      registry.recordProvenStart(databasePath, password);

      expect(config.auth[otherDatabasePath]).toBe(otherEntry);
      expect(config.auth[otherDatabasePath]).toEqual(storedScryptEntry());
    });
  });

  describe('with a stored scrypt record', () => {
    /** @type {AuthEntry} */
    let storedEntry;

    beforeEach(() => {
      storedEntry = storedScryptEntry();
      config.auth[databasePath] = storedEntry;
    });

    it('reads the database as scrypt', () => {
      expect(registry.stateOf(databasePath)).toBe('scrypt');
    });

    it('accepts the correct password', () => {
      expect(registry.verify(databasePath, STORED_SCRYPT_PASSWORD)).toBe(true);
    });

    it('rejects a wrong password', () => {
      expect(registry.verify(databasePath, 'hunter3')).toBe(false);
    });

    it('leaves the record untouched on a later proven start', () => {
      registry.recordProvenStart(databasePath, 'hunter3');

      expect(config.auth[databasePath]).toBe(storedEntry);
      expect(config.auth[databasePath]).toEqual(storedScryptEntry());
    });

    it('saves nothing on a later proven start', () => {
      registry.recordProvenStart(databasePath, 'hunter3');

      expect(save).not.toHaveBeenCalled();
    });
  });

  describe('with a stored passwordless marker', () => {
    /** @type {AuthEntry} */
    let storedEntry;

    beforeEach(() => {
      storedEntry = {
        passwordless: true
      };
      config.auth[databasePath] = storedEntry;
    });

    it('reads the database as passwordless', () => {
      expect(registry.stateOf(databasePath)).toBe('passwordless');
    });

    it('leaves the marker untouched on a later proven start', () => {
      registry.recordProvenStart(databasePath, 'hunter2');

      expect(config.auth[databasePath]).toBe(storedEntry);
      expect(config.auth[databasePath]).toEqual({passwordless: true});
    });

    it('saves nothing on a later proven start', () => {
      registry.recordProvenStart(databasePath, '');

      expect(save).not.toHaveBeenCalled();
    });
  });

  describe('with a mangled entry', () => {
    beforeEach(() => {
      config.auth[databasePath] = {scrypt: {cost: 16384}};
    });

    it('reads the database as pending', () => {
      expect(registry.stateOf(databasePath)).toBe('pending');
    });

    it('replaces the entry with a freshly derived scrypt record', () => {
      registry.recordProvenStart(databasePath, password);

      expect(config.auth[databasePath]).toEqual({
        scrypt: {
          salt: base64Of(SALT_BYTES),
          hash: base64Of(HASH_BYTES),
          cost: 16384,
          blockSize: 8,
          parallelization: 1
        }
      });
    });

    it('saves the replacement', () => {
      registry.recordProvenStart(databasePath, password);

      expect(save).toHaveBeenCalledTimes(1);
      expect(save).toHaveBeenCalledWith(config);
    });
  });
});
