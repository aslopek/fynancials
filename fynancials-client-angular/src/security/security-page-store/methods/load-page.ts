import {WritableSignalStore} from '../../../common/types/signal-store.type';
import {SearchParams, SecurityPageState} from '../security-page.store';
import {Store} from '@ngrx/store';
import {AppState} from '../../../store/app.state';
import {PaginatedSecurityRead, SecurityApi} from '../../../gen/api/security';
import {catchError, EMPTY, take} from 'rxjs';
import {SecurityActions} from '../../../store/security/security.actions';
import {patchState} from '@ngrx/signals';

export function loadPage(signalStore: WritableSignalStore<SecurityPageState>,
                         globalStore: Store<AppState>,
                         securityApi: SecurityApi,
                         pageIndex: number): void {
  const searchParams: SearchParams = signalStore.searchParams();

  securityApi.getSecurities(pageIndex, undefined, undefined, searchParams.search ?? undefined)
    .pipe(
      take(1),
      catchError(() => EMPTY)
    ).subscribe((page: PaginatedSecurityRead): void => {
    globalStore.dispatch(SecurityActions.setSecurities({securities: page.items}));
    patchState(signalStore, {
      page
    });
  });
}