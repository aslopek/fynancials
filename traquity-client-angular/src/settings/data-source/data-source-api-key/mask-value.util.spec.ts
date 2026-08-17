import {beforeEach, describe, expect, it} from '@jest/globals';
import {UrlPattern} from '../data-source.type';
import {extractMaskedValue, findMaskedValue, replaceMaskedValue} from './mask-value.util';

describe('extractMaskedValue', (): void => {

  let urlPattern: string;

  beforeEach((): void => {
    urlPattern = 'https://stock.api/#id()/prices?key=#mask(someSecretApiKey)';
  });

  it('returns the argument of the masked value', (): void => {
    expect(extractMaskedValue(urlPattern)).toBe('someSecretApiKey');
  });

  it('returns null when no value is masked', (): void => {
    urlPattern = 'https://stock.api/#id()/prices';

    expect(extractMaskedValue(urlPattern)).toBeNull();
  });

  it('returns the empty argument of an empty masked value', (): void => {
    urlPattern = 'https://stock.api/prices?key=#mask()';

    expect(extractMaskedValue(urlPattern)).toBe('');
  });

  it('returns an argument containing a parenthesized group', (): void => {
    urlPattern = 'https://stock.api/prices?key=#mask(#base64(secret))';

    expect(extractMaskedValue(urlPattern)).toBe('#base64(secret)');
  });

  it('returns the argument of the first masked value', (): void => {
    urlPattern = 'https://stock.api/prices?key=#mask(first)&other=#mask(second)';

    expect(extractMaskedValue(urlPattern)).toBe('first');
  });

  // the argument is long enough that a pattern backtracking exponentially over it would not finish, so this also guards the linear parse
  it('returns null for an unterminated masked value', (): void => {
    urlPattern = `https://stock.api/prices?key=#mask(${"'".repeat(40)}`;

    expect(extractMaskedValue(urlPattern)).toBeNull();
  });
});

describe('replaceMaskedValue', (): void => {

  let urlPattern: string;

  beforeEach((): void => {
    urlPattern = 'https://stock.api/#id()/prices?key=#mask(someSecretApiKey)';
  });

  it('replaces the argument of the masked value', (): void => {
    expect(replaceMaskedValue(urlPattern, 'newKey')).toBe('https://stock.api/#id()/prices?key=#mask(newKey)');
  });

  it('returns the pattern unchanged when no value is masked', (): void => {
    urlPattern = 'https://stock.api/#id()/prices';

    expect(replaceMaskedValue(urlPattern, 'newKey')).toBe('https://stock.api/#id()/prices');
  });

  it('replaces only the first masked value', (): void => {
    urlPattern = 'https://stock.api/prices?key=#mask(first)&other=#mask(second)';

    expect(replaceMaskedValue(urlPattern, 'newKey')).toBe('https://stock.api/prices?key=#mask(newKey)&other=#mask(second)');
  });
});

describe('findMaskedValue', (): void => {

  let urlPatterns: UrlPattern[];

  beforeEach((): void => {
    urlPatterns = [
      {timespanInDays: 30, urlPattern: 'https://stock.api/#id()/prices'},
      {timespanInDays: 365, urlPattern: 'https://stock.api/#id()/prices?key=#mask(someSecretApiKey)'}
    ];
  });

  it('returns the masked value of the first pattern carrying one', (): void => {
    expect(findMaskedValue(urlPatterns)).toBe('someSecretApiKey');
  });

  it('returns null when no pattern carries a masked value', (): void => {
    urlPatterns = [{timespanInDays: 30, urlPattern: 'https://stock.api/#id()/prices'}];

    expect(findMaskedValue(urlPatterns)).toBeNull();
  });

  it('returns null for no patterns at all', (): void => {
    urlPatterns = [];

    expect(findMaskedValue(urlPatterns)).toBeNull();
  });
});
