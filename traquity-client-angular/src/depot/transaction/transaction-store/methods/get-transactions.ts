import {WritableSignalStore} from '../../../../common/types/signal-store.type';
import {TransactionPageState} from '../transaction-page.store';
import {Store} from '@ngrx/store';
import {AppState} from '../../../../store/app.state';
import {PaginatedTransactionRead, TransactionApi, TransactionType} from '../../../../gen/api/depot-transaction';
import {catchError, EMPTY, Observable, take} from 'rxjs';
import {patchState} from '@ngrx/signals';
import {SecurityActions} from '../../../../store/security/security.actions';
import {selectedDepotIds} from '../../../../store/depot/depot.selector';

export type GetTransactionArgs = {
  filteredSecurityIds: number[] | null
  filteredTransactionTypes: TransactionType[]
  page: number
};

export function getTransactions(signalStore: WritableSignalStore<TransactionPageState>,
                                globalStore: Store<AppState>,
                                transactionApi: TransactionApi,
                                args: GetTransactionArgs): void {
  const selectedDepotIdsValue: number[] = globalStore.selectSignal(selectedDepotIds)();
  if (selectedDepotIdsValue.length !== 1) {
    return;
  }

  const pageSize: number = signalStore.pageSize();
  const filteredSecurityIds: number[] | null = args.filteredSecurityIds;
  const filteredTransactionTypes: TransactionType[] = args.filteredTransactionTypes;

  if (filteredTransactionTypes.length === 0) {
    patchState(signalStore, {
      filteredSecurityIds,
      filteredTransactionTypes,
      transactionPage: {
        total: 0,
        currentPage: 0,
        lastPage: 0,
        pageSize,
        items: []
      }
    });
    return;
  }

  transactionApi.getTransactions(
    selectedDepotIdsValue[0],
    args.page,
    pageSize,
    'DESC',
    filteredTransactionTypes,
    undefined,
    undefined,
    filteredSecurityIds ?? undefined
  ).pipe(
    take(1),
    catchError((): Observable<never> => EMPTY)
  ).subscribe((transactionPage: PaginatedTransactionRead): void => {
    for (const transaction of transactionPage.items) {
      globalStore.dispatch(SecurityActions.loadSecurity({securityId: transaction.securityId}));
    }

    patchState(signalStore, {
      filteredSecurityIds,
      filteredTransactionTypes,
      transactionPage
    });
  });
}
