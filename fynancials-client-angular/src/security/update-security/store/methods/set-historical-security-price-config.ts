import {WritableSignalStore} from '../../../../common/types/signal-store.type';
import {UpdateSecurityState} from '../update-security.store';
import {HistoricalSecurityPriceConfig} from '../../../../gen/api/historical-security-price';
import {patchState} from '@ngrx/signals';

export function setHistoricalSecurityPriceConfig(signalStore: WritableSignalStore<UpdateSecurityState>,
                                                 config: Omit<HistoricalSecurityPriceConfig, 'version'> | null): void {
  patchState(signalStore, {
    historicalSecurityPriceConfig: config,
    historicalSecurityPriceConfigTouched: true
  });
}