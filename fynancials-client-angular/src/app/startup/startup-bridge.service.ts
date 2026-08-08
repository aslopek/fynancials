import {Inject, Injectable} from "@angular/core";
import {defer, from, Observable} from "rxjs";
import {BRIDGE_HOST, BridgeHost} from "./bridge-host.token";
import {BackendStartOutcome, FynancialsBridge, StartupState} from "./startup-bridge.type";

/**
 * The only place in the renderer that touches the `contextBridge` surface `preload.js` exposes. `#36`/`#37`/`#38`
 * add their own methods here rather than reaching into `window.fynancials` from elsewhere.
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

  private requireBridge(): FynancialsBridge {
    if (this.bridge == null) {
      throw new Error("The fynancials bridge is not available");
    }
    return this.bridge;
  }
}
