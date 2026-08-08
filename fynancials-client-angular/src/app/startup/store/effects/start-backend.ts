import {Router} from "@angular/router";
import {RxMethod, rxMethod} from "@ngrx/signals/rxjs-interop";
import {catchError, EMPTY, exhaustMap, Observable, pipe, tap} from "rxjs";
import {WritableSignalStore} from "../../../../common/types/signal-store.type";
import {StartupBridgeService} from "../../startup-bridge.service";
import {BackendStartOutcome} from "../../startup-bridge.type";
import {applyStartOutcome} from "../methods/apply-start-outcome";
import {enterBooting} from "../methods/enter-booting";
import {StartupComputed, StartupStoreState} from "../startup.store";

/**
 * No spec of its own: every decision this makes lives in a spec'd pure function (`enterBooting`,
 * `applyStartOutcome`, `phaseAfterFailedStart`/`startupRouteFor`), and the rest is `rxMethod` wiring.
 */
export function startBackend(signalStore: WritableSignalStore<StartupStoreState, StartupComputed>,
                             bridge: StartupBridgeService,
                             router: Pick<Router, 'navigate'>): RxMethod<string> {
  return rxMethod<string>(pipe(
    tap((): void => enterBooting(signalStore, router)),
    // exhaustMap mirrors the main process's own single-instance guard: a second call while one is in flight is
    // dropped rather than queued
    exhaustMap((password: string): Observable<BackendStartOutcome> => bridge.startBackend(password).pipe(
      catchError((error: unknown): Observable<never> => {
        // a rejected invoke is the main process refusing a second start while one runs - not a failed start,
        // so the phase must stay where it is
        return EMPTY;
      })
    )),
    tap((outcome: BackendStartOutcome): void => applyStartOutcome(signalStore, router, outcome))
  ));
}
