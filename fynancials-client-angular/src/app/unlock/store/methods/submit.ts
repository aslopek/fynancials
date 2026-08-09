import {ReadableSignalStore} from "../../../../common/types/signal-store.type";
import {ReadableStartupStore} from "../../../startup/store/startup.store";
import {UnlockComputed, UnlockState} from "../unlock.store";

/**
 * The single guarded entry point for starting the backend, used by both the OK button and the Enter key. The guard
 * is not redundant with `[disabled]` on the button: `[disabled]` stops the click, but nothing stops `keyup.enter` on
 * the input, so without this branch Enter would start a backend with an unverified password.
 */
export function submit(signalStore: ReadableSignalStore<Pick<UnlockState, 'password'>, Pick<UnlockComputed, 'canSubmit'>>,
                       startupStore: Pick<ReadableStartupStore, 'startBackend'>): void {
  if (!signalStore.canSubmit()) {
    return;
  }
  startupStore.startBackend(signalStore.password());
}
