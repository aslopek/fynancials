const {beforeEach, describe, expect, it, jest} = require('@jest/globals');
const crypto = require('node:crypto');
const {Readable} = require('node:stream');
const {verifyHash} = require('./verify-hash.js');

/** @import {VerifyHashOptions} from './verify-hash.js' */

describe('verifyHash', () => {
  const filePath = '/artifacts/model.gguf';
  const fileChunks = [Buffer.from('first chunk of the artifact'), Buffer.from('second chunk of the artifact')];
  const fileBytes = Buffer.concat(fileChunks);

  const {publicKey, privateKey} = crypto.generateKeyPairSync('rsa', {modulusLength: 2048});
  const {privateKey: otherPrivateKey} = crypto.generateKeyPairSync('rsa', {modulusLength: 2048});

  /** @type {jest.Mock<(filePath: string) => NodeJS.ReadableStream>} */
  let createReadStream;

  /** @type {VerifyHashOptions} */
  let options;

  beforeEach(() => {
    createReadStream = jest.fn(() => Readable.from(fileChunks));
    options = {
      filePath,
      signature: crypto.sign('RSA-SHA256', fileBytes, privateKey),
      publicKey,
      algorithm: 'RSA-SHA256',
      createReadStream
    };
  });

  it('verifies a signature over a file arriving in several chunks', async () => {
    await expect(verifyHash(options)).resolves.toBe(true);
  });

  it('streams the file it was given the path of', async () => {
    await verifyHash(options);

    expect(createReadStream).toHaveBeenCalledWith(filePath);
    expect(createReadStream).toHaveBeenCalledTimes(1);
  });

  it('rejects a single flipped byte in the file', async () => {
    const tampered = Buffer.from(fileBytes);
    tampered[0] = (tampered[0] ?? 0) ^ 0xff;
    createReadStream.mockReturnValue(Readable.from([tampered]));

    await expect(verifyHash(options)).resolves.toBe(false);
  });

  it('rejects a single flipped byte in the signature', async () => {
    const tampered = Buffer.from(options.signature);
    const lastIndex = tampered.length - 1;
    tampered[lastIndex] = (tampered[lastIndex] ?? 0) ^ 0xff;
    options.signature = tampered;

    await expect(verifyHash(options)).resolves.toBe(false);
  });

  it('rejects a signature made by a different key', async () => {
    options.signature = crypto.sign('RSA-SHA256', fileBytes, otherPrivateKey);

    await expect(verifyHash(options)).resolves.toBe(false);
  });

  it('hashes with the algorithm it was given rather than a fixed one', async () => {
    options.algorithm = 'RSA-SHA512';
    options.signature = crypto.sign('RSA-SHA512', fileBytes, privateKey);

    await expect(verifyHash(options)).resolves.toBe(true);
  });

  it('verifies under the padding it was given', async () => {
    options.padding = crypto.constants.RSA_PKCS1_PSS_PADDING;
    options.signature = crypto.sign('RSA-SHA256', fileBytes, {
      key: privateKey,
      padding: crypto.constants.RSA_PKCS1_PSS_PADDING
    });

    await expect(verifyHash(options)).resolves.toBe(true);
  });

  it('rejects a signature made under a padding other than the one it was given', async () => {
    options.padding = crypto.constants.RSA_PKCS1_PADDING;
    options.signature = crypto.sign('RSA-SHA256', fileBytes, {
      key: privateKey,
      padding: crypto.constants.RSA_PKCS1_PSS_PADDING
    });

    await expect(verifyHash(options)).resolves.toBe(false);
  });

  it('rejects rather than resolving false when the file cannot be read', async () => {
    createReadStream.mockReturnValue(Readable.from((async function* () {
      throw new Error('ENOENT');
    })()));

    await expect(verifyHash(options)).rejects.toThrow('ENOENT');
  });

  describe('with trailing chunks', () => {
    const trailingChunks = [Buffer.from('signed suffix'), Buffer.from([0x04, 0xff, 0x00, 0x0d])];

    beforeEach(() => {
      options.trailingChunks = trailingChunks;
      options.signature = crypto.sign('RSA-SHA256', Buffer.concat([fileBytes, ...trailingChunks]), privateKey);
    });

    it('hashes them after the file, so a signature covering both verifies', async () => {
      await expect(verifyHash(options)).resolves.toBe(true);
    });

    it('rejects the same signature once the trailing chunks are no longer hashed', async () => {
      options.trailingChunks = [];

      await expect(verifyHash(options)).resolves.toBe(false);
    });

    it('rejects the same signature when the trailing chunks arrive in the other order', async () => {
      options.trailingChunks = [...trailingChunks].reverse();

      await expect(verifyHash(options)).resolves.toBe(false);
    });
  });
});
