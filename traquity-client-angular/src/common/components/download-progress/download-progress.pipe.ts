import {Pipe, PipeTransform} from "@angular/core";
import {TqDecimalPipe} from "../../pipe/tq-decimal.pipe";
import {TqPercentPipe} from "../../pipe/tq-percent.pipe";
import {downloadPercentage} from "./download-progress.util";

/**
 * The one status line of a download: received size, total size, percentage, rate and remaining time, joined with
 * ` · `. Sizes and rate are always MiB, the remaining time always `mm:ss`, and a term whose figure is absent is
 * dropped rather than faked.
 */
@Pipe({name: "downloadProgress"})
export class DownloadProgressPipe implements PipeTransform {

  constructor(private readonly tqDecimalPipe: TqDecimalPipe, private readonly tqPercentPipe: TqPercentPipe) {
  }

  transform(receivedBytes: number | undefined, totalBytes: number | undefined, bytesPerSecond: number | undefined,
            secondsRemaining: number | undefined): string {
    const parts: string[] = [];
    const receivedMiB: number = this.toMiB(receivedBytes ?? 0);

    if (totalBytes != null) {
      parts.push(`${this.tqDecimalPipe.transform(receivedMiB, '1.0-0')} MiB of `
        + `${this.tqDecimalPipe.transform(this.toMiB(totalBytes), '1.0-0')} MiB`);
      if (totalBytes > 0) {
        parts.push(this.tqPercentPipe.transform(downloadPercentage(receivedBytes ?? 0, totalBytes), '1.0-0'));
      }
    } else {
      parts.push(`${this.tqDecimalPipe.transform(receivedMiB, '1.0-0')} MiB`);
    }

    if (bytesPerSecond != null) {
      parts.push(`${this.tqDecimalPipe.transform(bytesPerSecond / 1024 / 1024, '1.1-1')} MiB/s`);
    }

    if (secondsRemaining != null) {
      parts.push(`${this.formatDuration(secondsRemaining)} left`);
    }

    return parts.join(' · ');
  }

  private toMiB(bytes: number): number {
    return Math.round(bytes / 1024 / 1024);
  }

  private formatDuration(totalSeconds: number): string {
    const rounded: number = Math.round(totalSeconds);
    const minutes: number = Math.floor(rounded / 60);
    const seconds: number = rounded % 60;
    return `${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}`;
  }
}
