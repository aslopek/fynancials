import {WritableSignalStore} from '../../../../common/types/signal-store.type';
import {TransactionPageState} from '../transaction-page.store';
import {Store} from '@ngrx/store';
import {AppState} from '../../../../store/app.state';
import {TransactionApi, TransactionType} from '../../../../gen/api/depot-transaction';
import {getTransactions} from './get-transactions';

export function setFilteredTransactionTypes(signalStore: WritableSignalStore<TransactionPageState>,
                                            globalStore: Store<AppState>,
                                            transactionApi: TransactionApi,
                                            types: TransactionType[]): void {
  getTransactions(signalStore, globalStore, transactionApi, {
    filteredSecurityIds: signalStore.filteredSecurityIds(),
    filteredTransactionTypes: types,
    page: 0
  });
}
