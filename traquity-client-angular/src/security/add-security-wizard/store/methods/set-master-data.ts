import {WritableSignalStore} from '../../../../common/types/signal-store.type';
import {AddSecurityWizardState} from '../add-security-wizard.store';
import {SecurityCreate} from '../../../../gen/api/security';
import {patchState} from '@ngrx/signals';

export function setMasterData(signalStore: WritableSignalStore<AddSecurityWizardState>, masterData: SecurityCreate | null): void {
  if (masterData === null) {
    patchState(signalStore, {masterData});
    return;
  }
  if (masterData.wkn != null && masterData.wkn.length === 0) {
    delete masterData.wkn;
  }
  if (masterData.sector != null && masterData.sector.length === 0) {
    delete masterData.sector;
  }
  patchState(signalStore, {masterData});
}
