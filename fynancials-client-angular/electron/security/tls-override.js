/**
 * Whether the environment overrides TLS certificate verification. The app runs only when
 * `NODE_TLS_REJECT_UNAUTHORIZED` is absent or exactly `'1'` - the default, verifying behavior. Any other value,
 * including ones Node itself ignores (`'false'`, `'no'`, `''`), means somebody set out to change TLS behavior: no
 * trimming, no case folding, no truthiness, so the rule never needs to be qualified later.
 */

/**
 * The one variable this module reads. The key is optional because an environment carrying none of it is the ordinary
 * case, and declared as `string | undefined` on top of that, so an environment that has it set to nothing is one this
 * type can express too - the two are the same observation here, and the classification below treats them alike.
 *
 * @typedef {Object} TlsEnvironment
 * @property {string | undefined} [NODE_TLS_REJECT_UNAUTHORIZED]
 */

/**
 * @param {TlsEnvironment} environment
 * @returns {boolean}
 */
function isTlsOverridden(environment) {
  const value = environment.NODE_TLS_REJECT_UNAUTHORIZED;
  return value != null && value !== '1';
}

module.exports = {isTlsOverridden};
