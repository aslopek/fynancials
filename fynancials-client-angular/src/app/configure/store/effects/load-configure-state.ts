import {RxMethod, rxMethod} from "@ngrx/signals/rxjs-interop";
import {Observable, pipe, switchMap, tap} from "rxjs";
import {WritableSignalStore} from "../../../../common/types/signal-store.type";
import {StartupBridgeService} from "../../../startup/startup-bridge.service";
import {ConfigureState} from "../../../startup/startup-bridge.type";
import {ConfigureStoreState} from "../configure.store";
import {setConfigureState} from "../methods/set-configure-state";

export function loadConfigureState(signalStore: WritableSignalStore<ConfigureStoreState>,
                                   bridge: Pick<StartupBridgeService, 'getConfigureState'>): RxMethod<void> {
  return rxMethod<void>(loadConfigureStatePipe(signalStore, bridge));
}

export function loadConfigureStatePipe(signalStore: WritableSignalStore<ConfigureStoreState>,
                                       bridge: Pick<StartupBridgeService, 'getConfigureState'>):
  (source$: Observable<void>) => Observable<ConfigureState> {
  return pipe(
    switchMap((): Observable<ConfigureState> => bridge.getConfigureState()),
    tap((state: ConfigureState): void => setConfigureState(signalStore, state))
  );
}
