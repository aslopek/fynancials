/** @import {AuthEntry} from '../config/config-schema.js' */

/**
 * The password `storedScryptEntry()` verifies against. Generated once, outside any spec, rather than by calling
 * `createScryptRecord` from the spec's own arrange step - a spec that needs a stored password already in place
 * builds it from this literal instead of from the production code that also owns the assertion being tested.
 */
const STORED_SCRYPT_PASSWORD = 'hunter2';

/**
 * A fresh copy of a valid scrypt `AuthEntry` for `STORED_SCRYPT_PASSWORD`, with `auth.js`'s own default parameters.
 *
 * @returns {AuthEntry}
 */
function storedScryptEntry() {
  return {
    scrypt: {
      salt: 'Usok5VIcsy6+/VmdAZk3yA==',
      hash: '7f80hQNThdgScaI6rguGejNjr413NI4zgyg++x+20No=',
      cost: 16384,
      blockSize: 8,
      parallelization: 1
    }
  };
}

module.exports = {STORED_SCRYPT_PASSWORD, storedScryptEntry};
