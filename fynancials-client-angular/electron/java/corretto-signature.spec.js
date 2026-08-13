const {beforeEach, describe, expect, it, jest} = require('@jest/globals');
const crypto = require('node:crypto');
const {Readable} = require('node:stream');

// these specs run untransformed (`transform: {}`), so nothing hoists this call above the requires the way babel-jest
// would: the mock has to be registered before the module under test pulls the real one in
jest.mock('../security/verify-hash.js', () => ({verifyHash: jest.fn()}));

const {verifyHash} = require('../security/verify-hash.js');
const {verifyDetachedSignature} = require('./corretto-signature.js');

/** @import {CorrettoPublicKey} from './corretto-public-key.js' */
/** @import {DetachedSignatureVerification} from './corretto-signature.js' */
/** @import {VerifyHashOptions} from '../security/verify-hash.js' */

/** @typedef {(options: VerifyHashOptions) => Promise<boolean>} VerifyHash */

/**
 * @param {Buffer} bytes
 * @returns {Buffer}
 */
function encodeMpi(bytes) {
  let start = 0;
  while (start < bytes.length - 1 && bytes[start] === 0) {
    start++;
  }
  const trimmed = bytes.subarray(start);
  let bitLength = (trimmed.length - 1) * 8;
  let leadingByte = trimmed[0] ?? 0;
  while (leadingByte > 0) {
    bitLength++;
    leadingByte >>= 1;
  }
  const header = Buffer.alloc(2);
  header.writeUInt16BE(bitLength, 0);
  return Buffer.concat([header, trimmed]);
}

/**
 * @param {number} type
 * @param {Buffer} data
 * @returns {Buffer}
 */
function encodeSubpacket(type, data) {
  return Buffer.concat([Buffer.from([data.length + 1, type]), data]);
}

/**
 * @param {number} tag
 * @param {number} bodyLength
 * @returns {Buffer}
 */
function oldFormatPacketHeader(tag, bodyLength) {
  const header = Buffer.alloc(3);
  header[0] = 0x80 | (tag << 2) | 1;
  header.writeUInt16BE(bodyLength, 1);
  return header;
}

/**
 * @typedef {Object} BuiltSignature
 * @property {Buffer} packet the whole detached signature, as a `.sig` file carries it
 * @property {Buffer} signatureValue the RSA signature the packet embeds as an MPI
 * @property {Buffer} signedPortion the packet's version-through-hashed-subpackets prefix, itself part of the signed data
 * @property {Buffer} trailer the six-byte OpenPGP v4 trailer, likewise part of the signed data
 */

/**
 * Builds a binary detached OpenPGP signature packet by hand, in exactly the shape `gpg --detach-sign` produces (see
 * `corretto-signature.js`'s own header for the byte layout), but signed with a throwaway key this spec controls. The
 * pieces the packet is assembled from are returned alongside it, so an assertion can name the bytes that must reach
 * the hash instead of recomputing them.
 *
 * @param {{archiveBytes: Buffer, privateKey: import('node:crypto').KeyObject, issuerFingerprint: Buffer}} options
 * @returns {BuiltSignature}
 */
function buildSignaturePacket(options) {
  const {archiveBytes, privateKey, issuerFingerprint} = options;

  const hashedArea = encodeSubpacket(33, Buffer.concat([Buffer.from([4]), issuerFingerprint]));

  const prefix = Buffer.alloc(6);
  prefix[0] = 4;
  prefix[1] = 0x00;
  prefix[2] = 1;
  prefix[3] = 8;
  prefix.writeUInt16BE(hashedArea.length, 4);
  const signedPortion = Buffer.concat([prefix, hashedArea]);

  const trailer = Buffer.from([0x04, 0xff, 0, 0, 0, 0]);
  trailer.writeUInt32BE(signedPortion.length, 2);

  const dataToSign = Buffer.concat([archiveBytes, signedPortion, trailer]);
  const signatureValue = crypto.sign('RSA-SHA256', dataToSign, {key: privateKey, padding: crypto.constants.RSA_PKCS1_PADDING});

  const unhashedLength = Buffer.from([0, 0]);
  const quickCheck = Buffer.from([0, 0]);

  const body = Buffer.concat([signedPortion, unhashedLength, quickCheck, encodeMpi(signatureValue)]);
  return {
    packet: Buffer.concat([oldFormatPacketHeader(2, body.length), body]),
    signatureValue,
    signedPortion,
    trailer
  };
}

describe('verifyDetachedSignature', () => {
  const archivePath = '/downloads/amazon-corretto-25-x64-linux-jdk.tar.gz';
  const archiveBytes = Buffer.from('fixture archive content for corretto-signature.spec.js', 'utf8');
  const fingerprint = 'AA11BB22CC33DD44EE55FF66AA77BB88CC99DD00';
  const otherFingerprint = '112233445566778899AABBCCDDEEFF0011223344';

  const {publicKey, privateKey} = crypto.generateKeyPairSync('rsa', {modulusLength: 2048});

  /** @type {CorrettoPublicKey} */
  const pinnedKey = {key: publicKey, fingerprint};

  const validSignature = buildSignaturePacket({archiveBytes, privateKey, issuerFingerprint: Buffer.from(fingerprint, 'hex')});

  /** @type {jest.Mock<VerifyHash>} */
  let verifyHashMock;

  /** @type {jest.Mock<(filePath: string) => NodeJS.ReadableStream>} */
  let createReadStream;

  /** @type {DetachedSignatureVerification} */
  let verification;

  beforeEach(() => {
    verifyHashMock = /** @type {jest.Mock<VerifyHash>} */ (verifyHash);
    verifyHashMock.mockReset();
    verifyHashMock.mockResolvedValue(true);

    createReadStream = jest.fn(() => Readable.from([archiveBytes]));
    verification = {
      archivePath,
      signatureBytes: validSignature.packet,
      publicKey: pinnedKey,
      createReadStream
    };
  });

  it('hashes the archive, the signed portion and the trailer under the pinned key, and reports the verdict', async () => {
    await expect(verifyDetachedSignature(verification)).resolves.toBe(true);

    expect(verifyHashMock).toHaveBeenCalledTimes(1);
    expect(verifyHashMock).toHaveBeenCalledWith({
      filePath: archivePath,
      signature: validSignature.signatureValue,
      publicKey,
      algorithm: 'RSA-SHA256',
      padding: crypto.constants.RSA_PKCS1_PADDING,
      createReadStream,
      trailingChunks: [validSignature.signedPortion, validSignature.trailer]
    });
  });

  it('reports a signature that does not verify', async () => {
    verifyHashMock.mockResolvedValue(false);

    await expect(verifyDetachedSignature(verification)).resolves.toBe(false);
  });

  it('reports a verification that could not conclude as a failed one', async () => {
    verifyHashMock.mockRejectedValue(new Error('EIO: i/o error, read'));

    await expect(verifyDetachedSignature(verification)).resolves.toBe(false);
  });

  it('rejects a signature claiming a different issuer, hashing nothing', async () => {
    const otherIssuer = buildSignaturePacket({archiveBytes, privateKey, issuerFingerprint: Buffer.from(otherFingerprint, 'hex')});
    verification.signatureBytes = otherIssuer.packet;

    await expect(verifyDetachedSignature(verification)).resolves.toBe(false);
    expect(verifyHashMock).not.toHaveBeenCalled();
  });

  it('rejects a packet carrying no issuer fingerprint at all, hashing nothing', async () => {
    const withoutIssuer = Buffer.from(validSignature.packet);
    // the issuer fingerprint subpacket's type octet sits right behind the six-byte prefix and its length octet
    withoutIssuer[3 + 6 + 1] = 0;

    verification.signatureBytes = withoutIssuer;

    await expect(verifyDetachedSignature(verification)).resolves.toBe(false);
    expect(verifyHashMock).not.toHaveBeenCalled();
  });

  it('rejects a signature announcing an algorithm other than SHA-256, hashing nothing', async () => {
    const sha512 = Buffer.from(validSignature.packet);
    // the hash algorithm octet is the fourth of the packet body, which the three-byte old-format header precedes
    sha512[3 + 3] = 10;

    verification.signatureBytes = sha512;

    await expect(verifyDetachedSignature(verification)).resolves.toBe(false);
    expect(verifyHashMock).not.toHaveBeenCalled();
  });

  it('rejects a well-formed but non-OpenPGP buffer, hashing nothing', async () => {
    verification.signatureBytes = Buffer.from('not a signature');

    await expect(verifyDetachedSignature(verification)).resolves.toBe(false);
    expect(verifyHashMock).not.toHaveBeenCalled();
  });
});
