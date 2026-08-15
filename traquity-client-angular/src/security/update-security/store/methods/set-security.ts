import {WritableSignalStore} from '../../../../common/types/signal-store.type';
import {UpdateSecurityState} from '../update-security.store';
import {Store} from '@ngrx/store';
import {AppState} from '../../../../store/app.state';
import {getSecurity} from '../../../../store/security/security.selector';
import {SecurityRead} from '../../../../gen/api/security';
import {patchState} from '@ngrx/signals';

export function setSecurity(signalStore: WritableSignalStore<UpdateSecurityState>,
                            globalStore: Store<AppState>,
                            securityId: number): void {
  const security: SecurityRead | null = globalStore.selectSignal(getSecurity(securityId))();
  if (security === null) {
    return;
  }

  patchState(signalStore, {
    securityId,
    masterData: security,
    logo: null,
    masterDataTouched: false,
    logoTouched: false,
    historicalSecurityPriceConfig: null,
    historicalSecurityPriceConfigTouched: false,
    dividendAnnouncementConfig: null,
    dividendAnnouncementConfigTouched: false
  } satisfies UpdateSecurityState);
}