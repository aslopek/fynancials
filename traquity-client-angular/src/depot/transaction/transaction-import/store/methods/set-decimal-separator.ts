import {patchState} from '@ngrx/signals';
import {WritableSignalStore} from '../../../../../common/types/signal-store.type';
import {TransactionImportState} from '../transaction-import.store';

export function setDecimalSeparator(signalStore: WritableSignalStore<TransactionImportState>, decimalSeparator: ',' | '.'): void {
  patchState(signalStore, {decimalSeparator});
}
