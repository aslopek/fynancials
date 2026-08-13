const crypto = require('node:crypto');
const {pipeline} = require('node:stream/promises');

/**
 * Verifies a detached signature over a file's contents against a public key, hashing the file as a stream: bytes are
 * pulled from disk only as fast as the hash consumes them, so an artifact of any size is verified without ever being
 * held in memory.
 *
 * What gets hashed is the file's bytes followed by `trailingChunks`, in that order, and nothing else - a format whose
 * signed data is the payload plus a suffix of its own (an OpenPGP v4 signature's trailer, for instance) passes that
 * suffix in, and a format that signs the bare payload passes nothing. Since a signature is only as meaningful as the
 * bytes it was computed over, this module appends nothing implicitly.
 *
 * Only signature algorithms that hash incrementally can be used - which is every RSA and ECDSA combination
 * `node:crypto` names, and not Ed25519, whose scheme requires the whole message at once.
 */

/**
 * @typedef {Object} VerifyHashOptions
 * @property {string} filePath the file whose bytes the signature covers
 * @property {Buffer} signature the raw signature bytes, unarmored
 * @property {import('node:crypto').KeyObject} publicKey
 * @property {string} algorithm a `node:crypto` signature algorithm, e.g. `RSA-SHA256`
 * @property {(filePath: string) => NodeJS.ReadableStream} createReadStream
 * @property {Buffer[]} [trailingChunks] hashed after the file's own bytes, in the order given; none by default
 * @property {number} [padding] an RSA padding constant, e.g. `crypto.constants.RSA_PKCS1_PADDING`; the algorithm's own
 *   default applies when absent
 */

/**
 * The bytes to hash, as one sequence. A generator rather than a concatenation, so that no more than one chunk exists
 * in memory at a time and the trailing bytes reach the hash through the same backpressured path as the file's.
 *
 * @param {NodeJS.ReadableStream} fileStream
 * @param {Buffer[]} trailingChunks
 * @returns {AsyncGenerator<string | Buffer>}
 */
async function* hashedBytes(fileStream, trailingChunks) {
  yield* fileStream;
  yield* trailingChunks;
}

/**
 * Resolves whether the signature verifies over the file's bytes plus any trailing chunks. Rejects instead of resolving
 * `false` when the file cannot be read: an I/O failure is not a verdict on the signature, and the two must not be
 * confused for one another.
 *
 * @param {VerifyHashOptions} options
 * @returns {Promise<boolean>}
 */
async function verifyHash(options) {
  const {filePath, signature, publicKey, algorithm, createReadStream, trailingChunks = [], padding} = options;

  /** @type {import('node:crypto').Verify} */
  const verifier = crypto.createVerify(algorithm);
  // a pipeline rather than a `data` listener: the next chunk is pulled only once the verifier has taken the previous
  // one, so a file that reads faster than it hashes waits on the disk instead of queueing up in memory. A read error
  // propagates out of here, and the file handle is closed either way.
  await pipeline(hashedBytes(createReadStream(filePath), trailingChunks), verifier);

  return verifier.verify({key: publicKey, ...(padding == null ? {} : {padding})}, signature);
}

module.exports = {verifyHash};
