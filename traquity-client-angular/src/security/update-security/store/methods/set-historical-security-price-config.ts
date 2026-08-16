import {WritableSignalStore} from '../../../../common/types/signal-store.type';
import {UpdateSecurityState} from '../update-security.store';
import {HistoricalSecurityPriceConfigCreate} from '../../../../gen/api/historical-security-price';
import {patchState} from '@ngrx/signals';

export function setHistoricalSecurityPriceConfig(signalStore: WritableSignalStore<UpdateSecurityState>,
                                                 config: HistoricalSecurityPriceConfigCreate | null): void {
  patchState(signalStore, {
    historicalSecurityPriceConfig: config,
    historicalSecurityPriceConfigTouched: true
  });
}