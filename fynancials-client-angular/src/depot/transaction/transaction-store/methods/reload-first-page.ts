import {WritableSignalStore} from '../../../../common/types/signal-store.type';
import {TransactionPageState} from '../transaction-page.store';
import {Store} from '@ngrx/store';
import {AppState} from '../../../../store/app.state';
import {TransactionApi} from '../../../../gen/api/depot-transaction';
import {loadPage} from './load-page';

export function reloadFirstPage(signalStore: WritableSignalStore<TransactionPageState>,
                                globalStore: Store<AppState>,
                                transactionApi: TransactionApi): void {
  loadPage(signalStore, globalStore, transactionApi, 0);
}
