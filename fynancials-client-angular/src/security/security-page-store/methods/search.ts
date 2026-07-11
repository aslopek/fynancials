import {WritableSignalStore} from '../../../common/types/signal-store.type';
import {emptySearchParams, SearchParams, SecurityPageState} from '../security-page.store';
import {Store} from '@ngrx/store';
import {AppState} from '../../../store/app.state';
import {PaginatedSecurityRead, SecurityApi} from '../../../gen/api/security';
import {catchError, EMPTY, Observable, take} from 'rxjs';
import {SecurityActions} from '../../../store/security/security.actions';
import {patchState} from '@ngrx/signals';

export function search(signalStore: WritableSignalStore<SecurityPageState>,
                       globalStore: Store<AppState>,
                       securityApi: SecurityApi,
                       search?: string): void {
  const searchParams: SearchParams = {
    ...emptySearchParams,
    search: search ?? null
  };

  securityApi.getSecurities(0, undefined, undefined, searchParams.search ?? undefined)
    .pipe(
      take(1),
      catchError((): Observable<never> => EMPTY)
    ).subscribe((page: PaginatedSecurityRead): void => {
    globalStore.dispatch(SecurityActions.setSecurities({securities: page.items}));
    patchState(signalStore, {
      page,
      searchParams
    });
  });
}