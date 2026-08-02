const {afterEach, beforeEach, describe, expect, it, jest} = require('@jest/globals');
const {authStateOf, createScryptRecord, passwordlessEntry, verifyPassword} = require('./auth.js');
const {STORED_SCRYPT_PASSWORD, storedScryptEntry} = require('../testing/stored-scrypt-entry.js');

/** @import {AuthEntry, ScryptRecord} from './config-schema.js' */

describe('auth', () => {
  const password = STORED_SCRYPT_PASSWORD;

  /** @type {AuthEntry} */
  let entry;

  /** @type {unknown[][]} */
  let consoleCalls;

  beforeEach(() => {
    consoleCalls = [];
    jest.spyOn(console, 'debug').mockImplementation(recordConsoleCall);
    jest.spyOn(console, 'error').mockImplementation(recordConsoleCall);
    jest.spyOn(console, 'info').mockImplementation(recordConsoleCall);
    jest.spyOn(console, 'log').mockImplementation(recordConsoleCall);
    jest.spyOn(console, 'warn').mockImplementation(recordConsoleCall);

    entry = storedScryptEntry();
  });

  afterEach(() => {
    // AC8: the module logs nothing at all, so no output of it can carry a password
    expect(consoleCalls).toEqual([]);
    jest.restoreAllMocks();
  });

  it('accepts the password a record was created from', () => {
    expect(verifyPassword(entry, password)).toBe(true);
  });

  it('rejects a wrong password', () => {
    expect(verifyPassword(entry, 'hunter3')).toBe(false);
  });

  it('rejects a password differing only in a trailing space', () => {
    expect(verifyPassword(entry, `${password} `)).toBe(false);
  });

  it('verifies with the record\'s stored parameters rather than the current defaults', () => {
    const differentCost = {scrypt: {...scryptRecordOf(entry), cost: 8192}};

    expect(verifyPassword(differentCost, password)).toBe(false);
  });

  it('marks a password-protected database as scrypt', () => {
    expect(authStateOf(entry)).toBe('scrypt');
  });

  describe('createScryptRecord', () => {
    /** @type {AuthEntry} */
    let generatedEntry;

    beforeEach(() => {
      generatedEntry = createScryptRecord(password);
    });

    it('creates a record with a salt of at least 16 bytes', () => {
      expect(Buffer.from(scryptRecordOf(generatedEntry).salt, 'base64').length).toBeGreaterThanOrEqual(16);
    });

    it('creates a fresh salt and hash for every record of the same password', () => {
      const secondEntry = createScryptRecord(password);

      expect(scryptRecordOf(secondEntry).salt).not.toEqual(scryptRecordOf(generatedEntry).salt);
      expect(scryptRecordOf(secondEntry).hash).not.toEqual(scryptRecordOf(generatedEntry).hash);
    });

    it('persists the parameters it derived with', () => {
      expect(scryptRecordOf(generatedEntry)).toEqual({
        salt: scryptRecordOf(generatedEntry).salt,
        hash: scryptRecordOf(generatedEntry).hash,
        cost: 16384,
        blockSize: 8,
        parallelization: 1
      });
    });

    it('never writes the password into the record', () => {
      expect(JSON.stringify(generatedEntry)).not.toContain(password);
    });
  });

  describe('passwordless entry', () => {
    /** @type {AuthEntry} */
    let passwordless;

    beforeEach(() => {
      passwordless = passwordlessEntry();
    });

    it('is an explicit marker without any scrypt record', () => {
      expect(passwordless).toEqual({passwordless: true});
    });

    it('is classified as passwordless', () => {
      expect(authStateOf(passwordless)).toBe('passwordless');
    });

    it('verifies the empty password', () => {
      expect(verifyPassword(passwordless, '')).toBe(true);
    });

    it('rejects a non-empty password', () => {
      expect(verifyPassword(passwordless, 'x')).toBe(false);
    });
  });

  describe('unusable entries', () => {
    it.each([
      ['a missing entry', undefined],
      ['an empty entry', {}],
      ['an empty scrypt block', {scrypt: {}}],
      ['a passwordless marker set to false', {passwordless: false}],
      ['an entry carrying both keys', {scrypt: scryptRecord(), passwordless: true}],
      ['a missing salt', {scrypt: withoutKey(scryptRecord(), 'salt')}],
      ['a salt that is not base64', {scrypt: {...scryptRecord(), salt: 'not base64!'}}],
      ['a hash that is not base64', {scrypt: {...scryptRecord(), hash: 'not base64!'}}],
      ['a hash shorter than 16 bytes', {scrypt: {...scryptRecord(), hash: Buffer.alloc(15).toString('base64')}}],
      ['a hash longer than 64 bytes', {scrypt: {...scryptRecord(), hash: Buffer.alloc(65).toString('base64')}}],
      ['a cost that is not a power of two', {scrypt: {...scryptRecord(), cost: 16383}}],
      ['a cost exceeding the memory bound', {scrypt: {...scryptRecord(), cost: 2 ** 30}}],
      ['a block size exceeding the memory bound', {scrypt: {...scryptRecord(), blockSize: 2 ** 20}}],
      [
        'a block size exceeding the memory bound only once parallelization is counted in',
        {scrypt: {...scryptRecord(), cost: 2, blockSize: 1000000}}
      ],
      ['a cost too large for its block size', {scrypt: {...scryptRecord(), cost: 2 ** 20, blockSize: 1}}],
      ['a parallelization beyond the CPU bound', {scrypt: {...scryptRecord(), parallelization: 1024}}],
      ['a negative cost', {scrypt: {...scryptRecord(), cost: -1}}],
      ['an entry that is not an object', 'passwordless']
    ])('reads %s as pending', (_description, unusableEntry) => {
      expect(authStateOf(unusableEntry)).toBe('pending');
    });

    it.each([
      ['a missing entry', undefined],
      ['an empty scrypt block', {scrypt: {}}]
    ])('does not verify against %s', (_description, unusableEntry) => {
      expect(verifyPassword(unusableEntry, password)).toBe(false);
    });

    // Mangled copies of a record the password does verify against. scrypt cannot run any of these parameter sets, so
    // what they pin is that its failure never escapes verifyPassword - whether the resource bounds keep the record
    // away from it or the guarded derivation swallows the throw is deliberately not distinguished here.
    it.each([
      ['a cost that is not a power of two', {cost: 16383}],
      ['a cost too large for its block size', {cost: 2 ** 20, blockSize: 1}],
      ['a block size exceeding the memory bound only once parallelization is counted in', {cost: 2, blockSize: 1000000}]
    ])('rejects %s rather than throwing', (_description, mangledParameters) => {
      const mangledEntry = {scrypt: {...scryptRecordOf(entry), ...mangledParameters}};

      expect(verifyPassword(mangledEntry, password)).toBe(false);
    });
  });

  /**
   * @param {unknown[]} args
   * @returns {void}
   */
  function recordConsoleCall(...args) {
    consoleCalls.push(args);
  }

  /**
   * @param {AuthEntry} authEntry
   * @returns {ScryptRecord}
   */
  function scryptRecordOf(authEntry) {
    if (!('scrypt' in authEntry)) {
      throw new Error('expected an entry holding a scrypt record');
    }
    return authEntry.scrypt;
  }

  /**
   * A well-formed record to derive the malformed variants above from. Its hash belongs to no password - nothing here
   * verifies against it.
   *
   * @returns {ScryptRecord}
   */
  function scryptRecord() {
    return {
      salt: Buffer.alloc(16).toString('base64'),
      hash: Buffer.alloc(32).toString('base64'),
      cost: 16384,
      blockSize: 8,
      parallelization: 1
    };
  }

  /**
   * @param {ScryptRecord} record
   * @param {keyof ScryptRecord} key
   * @returns {Partial<ScryptRecord>}
   */
  function withoutKey(record, key) {
    const {[key]: _removed, ...remaining} = record;
    return remaining;
  }
});
