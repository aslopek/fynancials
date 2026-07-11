import {WritableSignalStore} from '../../../../common/types/signal-store.type';
import {TransactionPageState} from '../transaction-page.store';
import {PaginatedTransactionRead, TransactionRead} from '../../../../gen/api/depot-transaction';
import {patchState} from '@ngrx/signals';

export function replaceTransaction(signalStore: WritableSignalStore<TransactionPageState>, updated: TransactionRead): void {
  const transactionPage: PaginatedTransactionRead = signalStore.transactionPage();
  patchState(signalStore, {
    transactionPage: {
      ...transactionPage,
      items: transactionPage.items.map((item: TransactionRead): TransactionRead => item.id === updated.id ? updated : item)
    }
  });
}
