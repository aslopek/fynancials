import {describe, expect, it} from '@jest/globals';
import {JavaDownloadPhaseLabelPipe} from './java-download-phase-label.pipe';

describe('JavaDownloadPhaseLabelPipe', (): void => {
  const pipe: JavaDownloadPhaseLabelPipe = new JavaDownloadPhaseLabelPipe();

  it('labels the verifying phase', (): void => {
    expect(pipe.transform('verifying')).toBe('Verifying the downloaded archive…');
  });

  it('labels the extracting phase', (): void => {
    expect(pipe.transform('extracting')).toBe('Extracting…');
  });

  it('has no label for the downloading phase, so the byte-derived line takes over', (): void => {
    expect(pipe.transform('downloading')).toBeUndefined();
  });
});
