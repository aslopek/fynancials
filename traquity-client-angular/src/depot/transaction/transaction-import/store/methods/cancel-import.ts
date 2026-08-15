import {patchState} from '@ngrx/signals';
import {WritableSignalStore} from '../../../../../common/types/signal-store.type';
import {TransactionImportState} from '../transaction-import.store';

export function cancelImport(signalStore: WritableSignalStore<TransactionImportState>): void {
  patchState(signalStore, {cancelled: true});
}
