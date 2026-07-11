import {WritableSignalStore} from '../../../common/types/signal-store.type';
import {emptySearchParams, SecurityPageState} from '../security-page.store';
import {Store} from '@ngrx/store';
import {AppState} from '../../../store/app.state';
import {PaginatedSecurityRead, SecurityApi} from '../../../gen/api/security';
import {catchError, EMPTY, Observable, take} from 'rxjs';
import {SecurityActions} from '../../../store/security/security.actions';
import {patchState} from '@ngrx/signals';

export function initialize(signalStore: WritableSignalStore<SecurityPageState>,
                           globalStore: Store<AppState>,
                           securityApi: SecurityApi): void {
  securityApi.getSecurities(0).pipe(
    take(1),
    catchError((): Observable<never> => EMPTY)
  ).subscribe((page: PaginatedSecurityRead): void => {
    globalStore.dispatch(SecurityActions.setSecurities({securities: page.items}));
    patchState(signalStore, {
      page,
      searchParams: emptySearchParams,
      hoveredSecurityId: null
    } satisfies SecurityPageState);
  });
}