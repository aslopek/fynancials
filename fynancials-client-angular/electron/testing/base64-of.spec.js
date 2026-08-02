const {describe, expect, it} = require('@jest/globals');
const {base64Of} = require('./base64-of.js');

describe('base64Of', () => {
  it('matches a base64 string decoding to the given byte length', () => {
    expect(Buffer.alloc(16).toString('base64')).toEqual(base64Of(16));
  });

  it('rejects a base64 string decoding to a different byte length', () => {
    expect(Buffer.alloc(15).toString('base64')).not.toEqual(base64Of(16));
  });

  it('rejects a string that is not base64', () => {
    expect('not base64!').not.toEqual(base64Of(16));
  });

  it('rejects a non-string value', () => {
    expect(42).not.toEqual(base64Of(16));
  });

  it('renders the expected byte length via toString', () => {
    expect(base64Of(16).toString()).toBe('Base64Of(16)');
  });

  it('renders the expected byte length via toAsymmetricMatcher, which is what a failed diff calls', () => {
    expect(base64Of(16).toAsymmetricMatcher()).toBe('Base64Of(16)');
  });
});
