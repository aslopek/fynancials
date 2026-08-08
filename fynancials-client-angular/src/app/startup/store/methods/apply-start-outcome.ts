import {Router} from "@angular/router";
import {patchState} from "@ngrx/signals";
import {WritableSignalStore} from "../../../../common/types/signal-store.type";
import {BackendStartOutcome} from "../../startup-bridge.type";
import {phaseAfterFailedStart, startupRouteFor} from "../routing/startup-route";
import {StartupComputed, StartupPhase, StartupStoreState} from "../startup.store";

/**
 * On a reachable outcome this does nothing - the shell's own splash gate owns the handover to the app. A failed
 * start routes on the state it was started from.
 */
export function applyStartOutcome(signalStore: WritableSignalStore<StartupStoreState, StartupComputed>,
                                  router: Pick<Router, 'navigate'>, outcome: BackendStartOutcome): void {
  if (outcome.reachable) {
    return;
  }

  const phase: StartupPhase = phaseAfterFailedStart(outcome.startedFrom);
  patchState(signalStore, {phase});

  const route: string | null = startupRouteFor(phase);
  if (route != null) {
    router.navigate([route]);
  }
}
