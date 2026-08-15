import {beforeEach, describe, expect, it, jest} from '@jest/globals';
import {TqDecimalPipe} from '../../pipe/tq-decimal.pipe';
import {TqPercentPipe} from '../../pipe/tq-percent.pipe';
import {DownloadProgressPipe} from './download-progress.pipe';
import {downloadPercentage} from './download-progress.util';

jest.mock('./download-progress.util', () => ({
  downloadPercentage: jest.fn()
}));

const MIB: number = 1024 * 1024;

type DownloadPercentage = (receivedBytes: number | undefined, totalBytes: number | undefined) => number;
type TqDecimalTransform = (value: number, digitsInfo: string) => string;
type TqPercentTransform = (value: number, digitsInfo: string) => string;

describe('DownloadProgressPipe', (): void => {
  let pipe: DownloadProgressPipe;
  let downloadPercentageMock: jest.Mock<DownloadPercentage>;
  let tqDecimalTransformMock: jest.Mock<TqDecimalTransform>;
  let tqPercentTransformMock: jest.Mock<TqPercentTransform>;

  beforeEach((): void => {
    downloadPercentageMock = downloadPercentage as jest.Mock<DownloadPercentage>;
    downloadPercentageMock.mockReset();
    // deliberately not the fraction the figures below work out to, so a term rendered from the pipe's own arithmetic
    // instead of from the collaborator fails these assertions
    downloadPercentageMock.mockReturnValue(0.42);

    // each mock renders its arguments back verbatim, so an assertion on the composed string still pins down what
    // the pipe handed to its collaborators, without duplicating TqDecimalPipe/TqPercentPipe's own formatting logic
    tqDecimalTransformMock = jest.fn((value: number, digitsInfo: string): string => `${value}(${digitsInfo})`);
    tqPercentTransformMock = jest.fn((value: number, digitsInfo: string): string => `${value}(${digitsInfo})%`);

    pipe = new DownloadProgressPipe(
      {transform: tqDecimalTransformMock} as unknown as TqDecimalPipe,
      {transform: tqPercentTransformMock} as unknown as TqPercentPipe
    );
  });

  it('renders received size, total size, percentage, rate and remaining time', (): void => {
    const receivedBytes: number = 118 * MIB;
    const totalBytes: number = 188 * MIB;
    const bytesPerSecond: number = 4 * MIB;

    expect(pipe.transform(receivedBytes, totalBytes, bytesPerSecond, 17))
      .toBe('118(1.0-0) MiB of 188(1.0-0) MiB · 0.42(1.0-0)% · 4(1.1-1) MiB/s · 00:17 left');
  });

  it('renders the percentage of the figures it was given', (): void => {
    const receivedBytes: number = 118 * MIB;
    const totalBytes: number = 188 * MIB;

    pipe.transform(receivedBytes, totalBytes, undefined, undefined);

    expect(downloadPercentageMock).toHaveBeenCalledTimes(1);
    expect(downloadPercentageMock).toHaveBeenCalledWith(receivedBytes, totalBytes);
  });

  it('passes the fraction downloadPercentage computed straight to the percent pipe', (): void => {
    downloadPercentageMock.mockReturnValue(0.75);

    pipe.transform(10 * MIB, 20 * MIB, undefined, undefined);

    expect(tqPercentTransformMock).toHaveBeenCalledTimes(1);
    expect(tqPercentTransformMock).toHaveBeenCalledWith(0.75, '1.0-0');
  });

  it('drops the total size and percentage when the total is unknown', (): void => {
    const receivedBytes: number = 10 * MIB;
    const bytesPerSecond: number = MIB;

    expect(pipe.transform(receivedBytes, undefined, bytesPerSecond, 5)).toBe('10(1.0-0) MiB · 1(1.1-1) MiB/s · 00:05 left');
    expect(downloadPercentageMock).not.toHaveBeenCalled();
    expect(tqPercentTransformMock).not.toHaveBeenCalled();
  });

  it('drops the percentage for a zero total, rather than rendering the zero it would compute', (): void => {
    const receivedBytes: number = 10 * MIB;
    const bytesPerSecond: number = MIB;

    expect(pipe.transform(receivedBytes, 0, bytesPerSecond, 5)).toBe('10(1.0-0) MiB of 0(1.0-0) MiB · 1(1.1-1) MiB/s · 00:05 left');
    expect(downloadPercentageMock).not.toHaveBeenCalled();
    expect(tqPercentTransformMock).not.toHaveBeenCalled();
  });

  it('drops the remaining time when it is unknown', (): void => {
    const receivedBytes: number = 10 * MIB;
    const totalBytes: number = 20 * MIB;
    const bytesPerSecond: number = 1 * MIB;

    expect(pipe.transform(receivedBytes, totalBytes, bytesPerSecond, undefined))
      .toBe('10(1.0-0) MiB of 20(1.0-0) MiB · 0.42(1.0-0)% · 1(1.1-1) MiB/s');
  });

  it('drops the rate when it is unknown', (): void => {
    const receivedBytes: number = 10 * MIB;
    const totalBytes: number = 20 * MIB;

    expect(pipe.transform(receivedBytes, totalBytes, undefined, undefined)).toBe('10(1.0-0) MiB of 20(1.0-0) MiB · 0.42(1.0-0)%');
  });

  it('rounds sizes to whole MiB before formatting, and passes the rate through undivided beyond MiB/s', (): void => {
    const receivedBytes: number = 1.6 * MIB;
    const bytesPerSecond: number = 1.25 * MIB;

    pipe.transform(receivedBytes, undefined, bytesPerSecond, undefined);

    expect(tqDecimalTransformMock.mock.calls).toEqual([[2, '1.0-0'], [1.25, '1.1-1']]);
  });

  it('zero-pads minutes and seconds under ten', (): void => {
    const receivedBytes: number = 0;

    expect(pipe.transform(receivedBytes, undefined, undefined, 65)).toBe('0(1.0-0) MiB · 01:05 left');
  });

  it('formats a remaining time over an hour as minutes:seconds, not hours', (): void => {
    const receivedBytes: number = 0;

    expect(pipe.transform(receivedBytes, undefined, undefined, 7200)).toBe('0(1.0-0) MiB · 120:00 left');
  });

  it('treats a missing received byte count as zero', (): void => {
    expect(pipe.transform(undefined, undefined, undefined, undefined)).toBe('0(1.0-0) MiB');
  });
});
