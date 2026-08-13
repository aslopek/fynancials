const crypto = require('node:crypto');
const {verifyHash} = require('../security/verify-hash.js');

/** @import {CorrettoPublicKey} from './corretto-public-key.js' */

/**
 * Verifies a detached OpenPGP signature (RFC 4880 section 5.2) over an archive file against one pinned public key.
 * Parsing is strict on purpose: version 4, binary-document type, RSA, SHA-256 and an issuer fingerprint equal to the
 * pinned key's are all required, and anything else - including a well-formed signature from a different key - is
 * rejected outright rather than negotiated. No algorithm agility means no downgrade to argue about.
 *
 * The cryptography itself stays inside `security/verify-hash.js`; this module only decides which bytes get hashed and
 * which key verifies them. Both are all-or-nothing: the bytes handed over have to be the ones signed, all of
 * them and in their order, or the check fails. There is no partial credit and no half-verified archive, so a mistake
 * here turns every genuine download away rather than quietly letting a tampered one through - it shows up as nothing
 * installing, never as the wrong thing installing.
 *
 * Everything the packet itself can rule out is ruled out before the archive is opened, so a signature that was never
 * going to verify costs no read at all.
 */

/**
 * @typedef {Object} DetachedSignatureVerification
 * @property {string} archivePath the file whose bytes the signature covers
 * @property {Buffer} signatureBytes the detached signature's own bytes, as the `.sig` file carries them
 * @property {CorrettoPublicKey} publicKey
 * @property {(filePath: string) => NodeJS.ReadableStream} createReadStream
 */

/** @type {number} tag 2: signature packet */
const SIGNATURE_PACKET_TAG = 2;
/** @type {number} binary document signature */
const BINARY_DOCUMENT_SIGNATURE_TYPE = 0x00;
/** @type {number} RSA (sign-only or sign+encrypt) */
const RSA_ALGORITHM = 1;
/** @type {number} SHA-256 */
const SHA256_ALGORITHM = 8;
/** @type {number} issuer fingerprint subpacket (RFC 4880bis) */
const ISSUER_FINGERPRINT_SUBPACKET_TYPE = 33;

/**
 * One packet's tag and body, per RFC 4880 section 4.2 (both old- and new-format headers). A detached `.sig` file is
 * exactly one packet, so no caller here needs the end offset.
 *
 * @param {Buffer} data
 * @returns {{tag: number, body: Buffer}}
 */
function readPacket(data) {
  const first = data[0];
  if (first == null || (first & 0x80) === 0) {
    throw new Error('Not an OpenPGP packet');
  }

  if ((first & 0x40) !== 0) {
    const tag = first & 0x3f;
    const firstLengthByte = data[1];
    if (firstLengthByte == null) {
      throw new Error('Truncated packet header');
    }
    if (firstLengthByte < 192) {
      return {tag, body: data.subarray(2, 2 + firstLengthByte)};
    }
    if (firstLengthByte === 255) {
      const bodyLength = data.readUInt32BE(2);
      return {tag, body: data.subarray(6, 6 + bodyLength)};
    }
    throw new Error('Unsupported new-format packet length');
  }

  const tag = (first >> 2) & 0x0f;
  const lengthType = first & 0x03;
  if (lengthType === 0) {
    const bodyLength = data[1] ?? 0;
    return {tag, body: data.subarray(2, 2 + bodyLength)};
  }
  if (lengthType === 1) {
    const bodyLength = data.readUInt16BE(1);
    return {tag, body: data.subarray(3, 3 + bodyLength)};
  }
  if (lengthType === 2) {
    const bodyLength = data.readUInt32BE(1);
    return {tag, body: data.subarray(5, 5 + bodyLength)};
  }
  throw new Error('Indeterminate-length packets are not supported');
}

/**
 * One RFC 4880 section 3.2 multiprecision integer: a 2-byte bit count followed by its minimal big-endian bytes.
 *
 * @param {Buffer} data
 * @param {number} offset
 * @returns {{value: Buffer, nextOffset: number}}
 */
function readMpi(data, offset) {
  const bitLength = data.readUInt16BE(offset);
  const byteLength = Math.ceil(bitLength / 8);
  const valueStart = offset + 2;
  return {value: data.subarray(valueStart, valueStart + byteLength), nextOffset: valueStart + byteLength};
}

/**
 * One subpacket of a hashed or unhashed subpacket area (RFC 4880 section 5.2.3.1): a variable-length length octet
 * sequence, a type octet, and the subpacket's own data. The returned length includes the type octet, matching how the
 * length is encoded on the wire.
 *
 * @param {Buffer} area
 * @param {number} offset
 * @returns {{type: number, data: Buffer, nextOffset: number}}
 */
function readSubpacket(area, offset) {
  const firstLengthByte = area[offset];
  if (firstLengthByte == null) {
    throw new Error('Truncated subpacket');
  }
  let length;
  let lengthOctets;
  if (firstLengthByte < 192) {
    length = firstLengthByte;
    lengthOctets = 1;
  } else if (firstLengthByte < 255) {
    length = ((firstLengthByte - 192) << 8) + (area[offset + 1] ?? 0) + 192;
    lengthOctets = 2;
  } else {
    length = area.readUInt32BE(offset + 1);
    lengthOctets = 5;
  }
  const type = area[offset + lengthOctets];
  if (type == null) {
    throw new Error('Truncated subpacket');
  }
  const dataStart = offset + lengthOctets + 1;
  return {type, data: area.subarray(dataStart, offset + lengthOctets + length), nextOffset: offset + lengthOctets + length};
}

/**
 * The issuer fingerprint subpacket's 20-byte value (its first byte, a version marker, is dropped), or null when the
 * hashed area carries none.
 *
 * @param {Buffer} hashedArea
 * @returns {Buffer | null}
 */
function issuerFingerprintOf(hashedArea) {
  let offset = 0;
  while (offset < hashedArea.length) {
    const {type, data, nextOffset} = readSubpacket(hashedArea, offset);
    if (type === ISSUER_FINGERPRINT_SUBPACKET_TYPE && data.length === 21) {
      return data.subarray(1);
    }
    offset = nextOffset;
  }
  return null;
}

/**
 * Left-pads a signature's MPI value to the exact byte length PKCS#1 v1.5 verification expects: the RSA modulus size.
 * An MPI is allowed to be shorter than that whenever its value's leading bits happen to be zero.
 *
 * @param {Buffer} value
 * @param {number} modulusByteLength
 * @returns {Buffer}
 */
function leftPadded(value, modulusByteLength) {
  if (value.length >= modulusByteLength) {
    return value;
  }
  return Buffer.concat([Buffer.alloc(modulusByteLength - value.length), value]);
}

/**
 * Resolves whether the archive verifies against the pinned key. Any failure along the way - a malformed packet, an
 * unreadable archive - resolves `false` rather than rejecting: nothing about a Java runtime is worth installing on the
 * strength of a signature check that did not conclude.
 *
 * @param {DetachedSignatureVerification} verification
 * @returns {Promise<boolean>}
 */
async function verifyDetachedSignature(verification) {
  const {archivePath, signatureBytes, publicKey, createReadStream} = verification;
  try {
    const {tag, body} = readPacket(signatureBytes);
    if (tag !== SIGNATURE_PACKET_TAG) {
      return false;
    }

    const version = body[0];
    const signatureType = body[1];
    const publicKeyAlgorithm = body[2];
    const hashAlgorithm = body[3];
    if (version !== 4 || signatureType !== BINARY_DOCUMENT_SIGNATURE_TYPE || publicKeyAlgorithm !== RSA_ALGORITHM
      || hashAlgorithm !== SHA256_ALGORITHM) {
      return false;
    }

    const hashedLength = body.readUInt16BE(4);
    const signedPortion = body.subarray(0, 6 + hashedLength);
    const hashedArea = body.subarray(6, 6 + hashedLength);

    const issuerFingerprint = issuerFingerprintOf(hashedArea);
    if (issuerFingerprint == null || issuerFingerprint.toString('hex').toUpperCase() !== publicKey.fingerprint) {
      return false;
    }

    const afterHashed = 6 + hashedLength;
    const unhashedLength = body.readUInt16BE(afterHashed);
    const afterUnhashed = afterHashed + 2 + unhashedLength;
    // two bytes of left-16-bits-of-hash quick check follow, then the signature MPI itself
    const {value: signatureMpi} = readMpi(body, afterUnhashed + 2);

    const trailer = Buffer.from([0x04, 0xff, 0, 0, 0, 0]);
    trailer.writeUInt32BE(signedPortion.length, 2);

    const modulusByteLength = /** @type {number} */ (publicKey.key.asymmetricKeyDetails?.modulusLength) / 8;
    const signatureValue = leftPadded(signatureMpi, modulusByteLength);

    // the archive is a few hundred megabytes, so it is hashed straight off the disk, with the two OpenPGP pieces the
    // signature also covers following it into the same hash
    return await verifyHash({
      filePath: archivePath,
      signature: signatureValue,
      publicKey: publicKey.key,
      algorithm: 'RSA-SHA256',
      padding: crypto.constants.RSA_PKCS1_PADDING,
      createReadStream,
      trailingChunks: [signedPortion, trailer]
    });
  } catch {
    return false;
  }
}

module.exports = {verifyDetachedSignature};
