import {beforeEach, describe, expect, it, jest} from '@jest/globals';
import {PositionGroupTextPipe} from './position-group-text.pipe';
import {FyCurrencyPipe, FyPercentPipe} from '../../common';
import {PositionGroup} from '../../store/depot/position-grouping/position-group.type';

describe('PositionGroupTextPipe', (): void => {
  let group: PositionGroup;
  let fyCurrencyPipe: FyCurrencyPipe;
  let fyPercentPipe: FyPercentPipe;
  let pipe: PositionGroupTextPipe;

  beforeEach((): void => {
    group = {
      name: 'Technology',
      positions: [],
      buyInAbsolute: 5000,
      buyInRelative: 4000,
      currentSizeAbsolute: 6000,
      currentSizeRelative: 5604
    };

    fyCurrencyPipe = {
      transform: jest.fn((_value: string | number, _currencyCode: string): string => '$6,000.00')
    } as unknown as FyCurrencyPipe;
    fyPercentPipe = {
      transform: jest.fn((_value: string | number): string => '56.04%')
    } as unknown as FyPercentPipe;

    pipe = new PositionGroupTextPipe(fyCurrencyPipe, fyPercentPipe);
  });

  it.each([true, false] satisfies boolean[])
  ('returns the name and the relative size when includeAbsolute is false and hideAbsoluteValues is %s',
    (hideAbsoluteValues: boolean): void => {
      expect(pipe.transform(group, false, hideAbsoluteValues, false, 'USD')).toBe('Technology · 56.04%');
    });

  it('appends the absolute size when includeAbsolute is true and absolute values are not hidden', (): void => {
    expect(pipe.transform(group, false, false, true, 'USD')).toBe('Technology · 56.04% · $6,000.00');
  });

  it('omits the absolute size when hideAbsoluteValues is true, even though includeAbsolute is true', (): void => {
    expect(pipe.transform(group, false, true, true, 'USD')).toBe('Technology · 56.04%');
  });

  it('reads the buy-in basis when useBuyIn is true', (): void => {
    pipe.transform(group, true, false, true, 'USD');
    expect(fyPercentPipe.transform).toHaveBeenCalledWith(group.buyInRelative / 100);
    expect(fyCurrencyPipe.transform).toHaveBeenCalledWith(group.buyInAbsolute, 'USD');
  });

  it('reads the current-size basis when useBuyIn is false', (): void => {
    pipe.transform(group, false, false, true, 'USD');
    expect(fyPercentPipe.transform).toHaveBeenCalledWith(group.currentSizeRelative / 100);
    expect(fyCurrencyPipe.transform).toHaveBeenCalledWith(group.currentSizeAbsolute, 'USD');
  });
});
