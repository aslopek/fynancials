import {UpdateSecurityState} from '../update-security.store';
import {ReadableSignalStore} from '../../../../common/types/signal-store.type';

export function masterDataValid(signalStore: ReadableSignalStore<UpdateSecurityState>): boolean {
  return signalStore.masterData() !== null;
}
