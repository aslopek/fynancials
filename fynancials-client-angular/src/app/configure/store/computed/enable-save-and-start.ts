import {computed, Signal} from "@angular/core";

/**
 * The frame's aggregate: one term per section, so that a section reporting itself incomplete blocks finishing without
 * any other section knowing it exists. Story #38's Java section adds `&& javaValid()` here, and nothing else in the
 * frame changes.
 */
export function enableSaveAndStart(databaseValid: Signal<boolean>): Signal<boolean> {
  return computed((): boolean => databaseValid());
}
