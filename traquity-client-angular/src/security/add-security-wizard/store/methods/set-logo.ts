import {WritableSignalStore} from '../../../../common/types/signal-store.type';
import {AddSecurityWizardState} from '../add-security-wizard.store';
import {patchState} from '@ngrx/signals';

export function setLogo(signalStore: WritableSignalStore<AddSecurityWizardState>, logo: File): void {
  patchState(signalStore, {logo});
}
