import {InjectionToken} from "@angular/core";
import {TraQuityBridge} from "./startup-bridge.type";

export type BridgeHost = { traquity?: TraQuityBridge };

// read through `globalThis` rather than referencing `window` directly - the Angular suite runs with
// `testEnvironment: 'node'`, where a bare `window` reference throws
export const BRIDGE_HOST: InjectionToken<BridgeHost> = new InjectionToken<BridgeHost>('BRIDGE_HOST', {
  providedIn: 'root',
  factory: (): BridgeHost => globalThis as BridgeHost
});
