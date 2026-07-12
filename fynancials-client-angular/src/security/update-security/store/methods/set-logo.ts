import {WritableSignalStore} from '../../../../common/types/signal-store.type';
import {patchState} from '@ngrx/signals';
import {UpdateSecurityState} from '../update-security.store';

export function setLogo(signalStore: WritableSignalStore<UpdateSecurityState>, logo: File): void {
  patchState(signalStore, {logo, logoTouched: true});
}
