import {patchState} from "@ngrx/signals";
import {WritableSignalStore} from "../../../../common/types/signal-store.type";
import {ConfigureState} from "../../../startup/startup-bridge.type";
import {ConfigureStoreState} from "../configure.store";

export function setConfigureState(signalStore: WritableSignalStore<ConfigureStoreState>,
                                  state: ConfigureState): void {
  patchState(signalStore, {
    configFileState: state.configFileState,
    knownDatabases: state.knownDatabases,
    logPath: state.logPath
  });
}
