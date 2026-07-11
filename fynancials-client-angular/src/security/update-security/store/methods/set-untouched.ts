import {WritableSignalStore} from '../../../../common/types/signal-store.type';
import {UpdateSecurityState} from '../update-security.store';
import {patchState} from '@ngrx/signals';

export function setUntouched(signalStore: WritableSignalStore<UpdateSecurityState>): void {
  patchState(signalStore, {
    masterDataTouched: false,
    historicalSecurityPriceConfigTouched: false,
    dividendAnnouncementConfigTouched: false
  });
}
