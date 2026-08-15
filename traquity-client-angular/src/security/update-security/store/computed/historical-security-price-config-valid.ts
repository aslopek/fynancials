import {ReadableSignalStore} from '../../../../common/types/signal-store.type';
import {UpdateSecurityState} from '../update-security.store';

export function historicalSecurityPriceConfigValid(signalStore: ReadableSignalStore<UpdateSecurityState>): boolean {
  return signalStore.historicalSecurityPriceConfig() !== null;
}
