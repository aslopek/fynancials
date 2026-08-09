import {Router} from "@angular/router";
import {patchState} from "@ngrx/signals";
import {WritableSignalStore} from "../../../../common/types/signal-store.type";
import {startupRouteFor} from "../routing/startup-route";
import {StartupComputed, StartupStoreState} from "../startup.store";

/**
 * Touches no bridge - AC16's "no backend spawned, no config written" holds by construction, since this method never
 * reaches `StartupBridgeService`.
 *
 * Clears `startFailed` on the way out, the way `enterBooting` does: the flag belongs to the start attempt that set
 * it, and both startup screens render that one flag rather than each carrying their own, so leaving it set would
 * show the unlock screen's failure on a configuration screen the user reached deliberately. A failed start routing
 * itself to `configure` goes through `applyStartOutcome`, which sets the flag there on purpose.
 */
export function enterConfigure(signalStore: WritableSignalStore<StartupStoreState, StartupComputed>,
                               router: Pick<Router, 'navigate'>): void {
  patchState(signalStore, {phase: 'configure', startFailed: false});
  const route: string | null = startupRouteFor('configure');
  if (route != null) {
    router.navigate([route]);
  }
}
