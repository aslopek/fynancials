import {WritableSignalStore} from '../../../../common/types/signal-store.type';
import {AddSecurityWizardState} from '../add-security-wizard.store';
import {HistoricalSecurityPriceConfig} from '../../../../gen/api/historical-security-price';
import {patchState} from '@ngrx/signals';

export function setHistoricalSecurityPriceConfig(signalStore: WritableSignalStore<AddSecurityWizardState>,
                                                 config: Omit<HistoricalSecurityPriceConfig, 'version'> | null): void {
  patchState(signalStore, {
    historicalSecurityPriceConfig: config,
    historicalSecurityPriceConfigStepOk: config !== null
  });
}
