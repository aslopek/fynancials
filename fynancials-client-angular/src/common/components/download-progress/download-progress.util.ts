export function isDownloadIndeterminate(receivedBytes: number | undefined, totalBytes: number | undefined): boolean {
  return receivedBytes == null || totalBytes == null;
}

/** The fraction received of the total, which is the scale a percent formatter takes. */
export function downloadPercentage(receivedBytes: number | undefined, totalBytes: number | undefined): number {
  if (receivedBytes == null || totalBytes == null || totalBytes === 0) {
    return 0;
  }
  return receivedBytes / totalBytes;
}

/** The progress on the 0-to-100 scale a Material progress bar's `value` is on. */
export function downloadBarValue(receivedBytes: number | undefined, totalBytes: number | undefined): number {
  return downloadPercentage(receivedBytes, totalBytes) * 100;
}
