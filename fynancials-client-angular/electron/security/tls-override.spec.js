const {describe, expect, it} = require('@jest/globals');
const {isTlsOverridden} = require('./tls-override.js');

describe('isTlsOverridden', () => {
  it('is not overridden when the variable is absent', () => {
    expect(isTlsOverridden({NODE_TLS_REJECT_UNAUTHORIZED: undefined})).toBe(false);
    expect(isTlsOverridden({})).toBe(false);
  });

  it('is not overridden when the variable is exactly 1', () => {
    expect(isTlsOverridden({NODE_TLS_REJECT_UNAUTHORIZED: '1'})).toBe(false);
  });

  it.each([
    ['0', '0'],
    ['the empty string', ''],
    ['a value Node itself ignores', 'false']
  ])('is overridden when the variable is %s', (_description, value) => {
    expect(isTlsOverridden({NODE_TLS_REJECT_UNAUTHORIZED: value})).toBe(true);
  });
});
