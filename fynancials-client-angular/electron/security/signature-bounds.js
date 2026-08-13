/**
 * How long a detached signature is allowed to be, wherever one arrives from outside the program as text. A signature
 * is a fixed-size artifact rather than a document - an RSA-4096 packet stays under a kilobyte of bytes, so its base64
 * form stays under two thousand characters - and nothing genuine grows past that. What the bound keeps out is a
 * sender deciding how much a process that stores, validates or forwards the value has to hold.
 *
 * It lives here rather than in one of the schemas that applies it because it is one property of one artifact: two
 * copies of the number are two bounds that can drift apart while continuing to describe the same signature.
 */

/** @type {number} the most a signature's base64 form may amount to, in characters */
const MAXIMUM_SIGNATURE_LENGTH = 8192;

module.exports = {MAXIMUM_SIGNATURE_LENGTH};
