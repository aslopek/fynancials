import {Router} from "@angular/router";
import {patchState} from "@ngrx/signals";
import {WritableSignalStore} from "../../../../common/types/signal-store.type";
import {StartupComputed, StartupStoreState} from "../startup.store";

/**
 * Navigates back to the shell only when the phase was not already `booting` - which is what keeps the boot path
 * (a passwordless database) from firing a redundant navigation into the router's own initial navigation, while the
 * unlock/configure path leaves its screen and lands on the splash screen.
 */
export function enterBooting(signalStore: WritableSignalStore<StartupStoreState, StartupComputed>,
                             router: Pick<Router, 'navigate'>): void {
  const wasAlreadyBooting: boolean = signalStore.phase() === 'booting';
  patchState(signalStore, {phase: 'booting', startFailed: false});
  if (!wasAlreadyBooting) {
    router.navigate(['/']);
  }
}
