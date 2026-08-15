import {RxMethod, rxMethod} from '@ngrx/signals/rxjs-interop';
import {pipe, tap} from 'rxjs';
import {Store} from '@ngrx/store';
import {AppState} from '../../../../store/app.state';
import {TransactionApi} from '../../../../gen/api/depot-transaction';
import {WritableSignalStore} from '../../../../common/types/signal-store.type';
import {TransactionPageState} from '../transaction-page.store';
import {loadPage} from '../methods/load-page';

export function reloadOnDepotChange(signalStore: WritableSignalStore<TransactionPageState>,
                                    globalStore: Store<AppState>,
                                    transactionApi: TransactionApi): RxMethod<number[]> {
  return rxMethod<number[]>(
    pipe(
      tap((): void => loadPage(signalStore, globalStore, transactionApi, 0))
    )
  );
}
