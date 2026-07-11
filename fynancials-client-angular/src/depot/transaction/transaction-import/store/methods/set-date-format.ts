import {patchState} from '@ngrx/signals';
import {WritableSignalStore} from '../../../../../common/types/signal-store.type';
import {TransactionImportState} from '../transaction-import.store';

export function setDateFormat(signalStore: WritableSignalStore<TransactionImportState>, dateFormat: string | null): void {
  patchState(signalStore, {dateFormat});
}
