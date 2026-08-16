import {beforeEach, describe, expect, it} from '@jest/globals';
import {SecurityTypeDisplayNamePipe} from './security-type-display-name.pipe';
import {SecurityType} from '../../gen/api/security';

describe('SecurityTypeDisplayNamePipe', (): void => {
  let pipe: SecurityTypeDisplayNamePipe;

  beforeEach((): void => {
    pipe = new SecurityTypeDisplayNamePipe();
  });

  it('returns the human-readable name for a stock', (): void => {
    expect(pipe.transform(SecurityType.STOCK)).toBe('Stock');
  });

  it('returns the human-readable name for an ETF', (): void => {
    expect(pipe.transform(SecurityType.ETF)).toBe('ETF');
  });

  it('returns the human-readable name for another security type', (): void => {
    expect(pipe.transform(SecurityType.OTHER)).toBe('Other');
  });

  it('returns an empty string when the security type is undefined', (): void => {
    expect(pipe.transform(undefined)).toBe('');
  });

  it('returns an empty string when the security type is null', (): void => {
    expect(pipe.transform(null)).toBe('');
  });
});
