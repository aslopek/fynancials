import {describe, expect, it} from '@jest/globals';
import {downloadBarValue, downloadPercentage, isDownloadIndeterminate} from './download-progress.util';

describe('isDownloadIndeterminate', (): void => {
  it('is determinate once both figures are known', (): void => {
    expect(isDownloadIndeterminate(10, 20)).toBe(false);
  });

  it('is indeterminate without a received byte count', (): void => {
    expect(isDownloadIndeterminate(undefined, 20)).toBe(true);
  });

  it('is indeterminate without a total byte count', (): void => {
    expect(isDownloadIndeterminate(10, undefined)).toBe(true);
  });

  it('is indeterminate with neither figure known', (): void => {
    expect(isDownloadIndeterminate(undefined, undefined)).toBe(true);
  });
});

describe('downloadPercentage', (): void => {
  it('computes the fraction of received over total, leaving rounding to the percent pipe', (): void => {
    expect(downloadPercentage(1, 3)).toBe(1 / 3);
  });

  it('is zero without a received byte count', (): void => {
    expect(downloadPercentage(undefined, 200)).toBe(0);
  });

  it('is zero without a total byte count', (): void => {
    expect(downloadPercentage(50, undefined)).toBe(0);
  });

  it('is zero rather than dividing by zero for a zero total', (): void => {
    expect(downloadPercentage(0, 0)).toBe(0);
  });
});

describe('downloadBarValue', (): void => {
  it('scales the progress onto the bar\'s 0-to-100 range', (): void => {
    expect(downloadBarValue(1, 4)).toBe(25);
  });

  it('is 100 for a completed download, which is the bar\'s full range', (): void => {
    expect(downloadBarValue(200, 200)).toBe(100);
  });

  it('is zero without a total byte count', (): void => {
    expect(downloadBarValue(50, undefined)).toBe(0);
  });
});
