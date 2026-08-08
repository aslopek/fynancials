import {AuthState} from "../../startup-bridge.type";
import {StartupPhase} from "../startup.store";

/**
 * The single source of truth for "which URL belongs to which phase" - the route guard and the store's own
 * navigation both go through this rather than encoding the mapping twice.
 */
export function startupRouteFor(phase: StartupPhase): string | null {
  switch (phase) {
    case "booting":
      return null;
    case "unlock":
      return "/unlock";
    case "configure":
      return "/configure";
  }
}

/**
 * A pending database (no record yet) goes back to the unlock screen, while a verified `scrypt` entry or a `passwordless` database goes to
 * the configuration screen.
 */
export function phaseAfterFailedStart(startedFrom: AuthState): StartupPhase {
  return startedFrom === "pending" ? "unlock" : "configure";
}
