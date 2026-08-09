import {Inject, Injectable} from "@angular/core";
import {defer, from, Observable} from "rxjs";
import {BRIDGE_HOST, BridgeHost} from "./bridge-host.token";
import {
  AppliedConfiguration,
  BackendStartOutcome,
  ConfigurationChanges,
  ConfigureState,
  FynancialsBridge,
  PickedDatabase,
  StartupState
} from "./startup-bridge.type";

/**
 * The only place in the renderer that touches the `contextBridge` surface exposed by `preload.js`. A further bridge
 * method is added here as another wrapper, never by reaching into `window.fynancials` from somewhere else.
 */
@Injectable({providedIn: "root"})
export class StartupBridgeService {

  private readonly bridge: FynancialsBridge | null;

  constructor(@Inject(BRIDGE_HOST) bridgeHost: BridgeHost) {
    this.bridge = bridgeHost.fynancials ?? null;
  }

  get available(): boolean {
    return this.bridge != null;
  }

  getStartupState(): Observable<StartupState> {
    return defer((): Observable<StartupState> => from(this.requireBridge().getStartupState()));
  }

  startBackend(password: string): Observable<BackendStartOutcome> {
    return defer((): Observable<BackendStartOutcome> => from(this.requireBridge().startBackend(password)));
  }

  verifyPassword(password: string): Observable<boolean> {
    return defer((): Observable<boolean> => from(this.requireBridge().verifyPassword(password)));
  }

  getConfigureState(): Observable<ConfigureState> {
    return defer((): Observable<ConfigureState> => from(this.requireBridge().getConfigureState()));
  }

  pickExistingDatabase(currentSelection: string | null): Observable<string | null> {
    return defer((): Observable<string | null> => from(this.requireBridge().pickExistingDatabase(currentSelection)));
  }

  pickNewDatabase(currentSelection: string | null): Observable<PickedDatabase | null> {
    return defer((): Observable<PickedDatabase | null> => from(this.requireBridge().pickNewDatabase(currentSelection)));
  }

  forgetPassword(databasePath: string): Observable<void> {
    return defer((): Observable<void> => from(this.requireBridge().forgetPassword(databasePath)));
  }

  applyConfiguration(changes: ConfigurationChanges): Observable<AppliedConfiguration> {
    return defer((): Observable<AppliedConfiguration> => from(this.requireBridge().applyConfiguration(changes)));
  }

  /**
   * Fire-and-forget: `ipcRenderer.send` returns synchronously and `app.quit()` is vetoable in Electron, so nothing
   * here may depend on the app still running afterward.
   */
  quit(): void {
    this.requireBridge().quit();
  }

  private requireBridge(): FynancialsBridge {
    if (this.bridge == null) {
      throw new Error("The fynancials bridge is not available");
    }
    return this.bridge;
  }
}
