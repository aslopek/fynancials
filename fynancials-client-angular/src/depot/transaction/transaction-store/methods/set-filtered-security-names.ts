import {WritableSignalStore} from '../../../../common/types/signal-store.type';
import {TransactionPageComputed, TransactionPageState} from '../transaction-page.store';
import {Store} from '@ngrx/store';
import {AppState} from '../../../../store/app.state';
import {TransactionApi} from '../../../../gen/api/depot-transaction';
import {getTransactions} from './get-transactions';

export function setFilteredSecurityNames(signalStore: WritableSignalStore<TransactionPageState, TransactionPageComputed>,
                                         globalStore: Store<AppState>,
                                         transactionApi: TransactionApi,
                                         names: string[]): void {
  const securityIdsByName: { [securityName: string]: number } = signalStore.securityIdsByName();
  const filteredSecurityIds: number[] | null = names.length > 0
    ? names.map((name: string): number => securityIdsByName[name])
    : null;

  getTransactions(signalStore, globalStore, transactionApi, {
    filteredSecurityIds,
    filteredTransactionTypes: signalStore.filteredTransactionTypes(),
    page: 0
  });
}
