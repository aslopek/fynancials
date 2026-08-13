const {describe, expect, it} = require('@jest/globals');
const {CORRETTO_PUBLIC_KEY} = require('./corretto-public-key.js');

describe('correttoPublicKey', () => {
  it('computes the pinned fingerprint from the pasted armor, so a bad paste fails here rather than at a download', () => {
    expect(CORRETTO_PUBLIC_KEY.fingerprint).toBe('6DC3636DAE534049C8B94623A122542AB04F24E3');
  });

  it('parses into an RSA-4096 public key with the standard public exponent', () => {
    expect(CORRETTO_PUBLIC_KEY.key.asymmetricKeyType).toBe('rsa');
    expect(CORRETTO_PUBLIC_KEY.key.asymmetricKeyDetails).toEqual({modulusLength: 4096, publicExponent: 65537n});
  });
});
