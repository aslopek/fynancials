import {RxMethod, rxMethod} from "@ngrx/signals/rxjs-interop";
import {firstValueFrom, pipe, switchMap} from "rxjs";
import {WritableSignalStore} from "../../../../common/types/signal-store.type";
import {DepotPerformanceComputed, DepotPerformanceState} from "../depot-performance.store";
import {PaginatedTransactionRead, SortOrder, TransactionApi, TransactionRead} from "../../../../gen/api/depot-transaction";
import {sortTransactions} from "../../../../common/functions/sort-transactions";
import {patchState} from "@ngrx/signals";
import {Store} from "@ngrx/store";
import {AppState} from "../../../../store/app.state";
import {SecurityActions} from "../../../../store/security/security.actions";
import {RebasedDepotValue} from "../computed/rebased-depot-value.type";

export type LoadTransactionsArgs = {
  depotIds: number[]
  depotValues: RebasedDepotValue[]
}

export function loadTransactions(signalStore: WritableSignalStore<DepotPerformanceState, DepotPerformanceComputed>,
                                 globalStore: Store<AppState>,
                                 transactionApi: TransactionApi): RxMethod<LoadTransactionsArgs> {
  const pageSize: number = 1000;
  return rxMethod<LoadTransactionsArgs>(
    pipe(
      switchMap(async (args: LoadTransactionsArgs): Promise<void> => {
        const {depotIds, depotValues} = args;
        if (depotValues.length === 0) {
          return;
        }

        const transactions: TransactionRead[] = [];
        let pageNumber: number;
        let page: PaginatedTransactionRead;
        for (const depotId of depotIds) {
          pageNumber = 0;
          do {
            try {
              page = await firstValueFrom(transactionApi.getTransactions(depotId, pageNumber, pageSize, undefined, SortOrder.ASC, depotValues[0].date));
              transactions.push(...page.items);
              pageNumber++;
            } catch (ignored) {
              patchState(signalStore, {transactions: []});
              return;
            }
          } while (page.lastPage >= pageNumber);
        }

        sortTransactions(transactions);
        transactions.reverse();
        patchState(signalStore, {transactions});

        const securityIds: Set<number> = new Set<number>();
        for (const transaction of transactions) {
          securityIds.add(transaction.securityId);
        }
        for (const securityId of securityIds) {
          globalStore.dispatch(SecurityActions.loadSecurity({securityId}));
        }
      })
    )
  )
}
