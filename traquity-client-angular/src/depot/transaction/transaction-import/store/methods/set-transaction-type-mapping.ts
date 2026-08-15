import {patchState} from '@ngrx/signals';
import {WritableSignalStore} from '../../../../../common/types/signal-store.type';
import {TransactionType} from '../../../../../gen/api/depot-transaction';
import {TransactionImportComputed, TransactionImportState} from '../transaction-import.store';

export function setTransactionTypeMapping(signalStore: WritableSignalStore<TransactionImportState, TransactionImportComputed>,
                                          csvValue: string, transactionType: TransactionType | null): void {
  patchState(signalStore, {
    transactionTypeMapping: {...signalStore.transactionTypeMapping(), [csvValue]: transactionType}
  });
}
