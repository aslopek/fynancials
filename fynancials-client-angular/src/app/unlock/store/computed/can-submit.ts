import {computed, Signal} from "@angular/core";
import {ReadableSignalStore} from "../../../../common/types/signal-store.type";
import {ReadableStartupStore} from "../../../startup/store/startup.store";
import {UnlockState} from "../unlock.store";

/**
 * A stored `scrypt` record is the only state that gates the button: `pending` and the defensive `null` case are
 * ungated, and `passwordless` never reaches this screen because its mode is `boot`.
 */
export function canSubmit(signalStore: ReadableSignalStore<Pick<UnlockState, 'passwordMatches'>>,
                          startupStore: Pick<ReadableStartupStore, 'authState'>): Signal<boolean> {
  return computed((): boolean => startupStore.authState() !== 'scrypt' || signalStore.passwordMatches());
}
