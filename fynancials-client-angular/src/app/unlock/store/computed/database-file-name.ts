import {computed, Signal} from "@angular/core";
import {ReadableStartupStore} from "../../../startup/store/startup.store";

/** `databasePath` is a base path without extension, produced on whichever platform wrote the config. */
export function databaseFileName(startupStore: Pick<ReadableStartupStore, 'databasePath'>): Signal<string> {
  return computed((): string => fileNameOf(startupStore.databasePath()));
}

function fileNameOf(databasePath: string | null): string {
  if (databasePath == null) {
    return '';
  }
  const segments: string[] = databasePath.split(/[\\/]/).filter((segment: string): boolean => segment.length > 0);
  return segments.length === 0 ? '' : segments[segments.length - 1];
}
