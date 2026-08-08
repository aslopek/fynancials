import {inject} from "@angular/core";
import {map, Observable, of, tap} from "rxjs";
import {StartupBridgeService} from "./startup-bridge.service";
import {StartupState} from "./startup-bridge.type";
import {ReadableStartupStore, StartupStore} from "./store/startup.store";

/**
 * Resolves the startup state from the bridge before the router's first navigation, registered via
 * `provideAppInitializer` in `app.config.ts`. Without that, the shell would mount first, start its backend poll,
 * and then be navigated away from - the double-dispatch the startup guard exists to avoid.
 */
export function initializeStartup(): Observable<void> {
  const bridge: StartupBridgeService = inject(StartupBridgeService);
  const store: ReadableStartupStore = inject(StartupStore);
  if (!bridge.available) {
    return of(undefined);
  }
  return bridge.getStartupState().pipe(
    tap((state: StartupState): void => store.setStartupState(state)),
    map((): void => undefined)
  );
}
