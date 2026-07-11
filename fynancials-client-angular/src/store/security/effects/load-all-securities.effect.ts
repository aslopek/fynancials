import {Actions, ofType} from '@ngrx/effects';
import {PaginatedSecurityRead, SecurityApi} from '../../../gen/api/security';
import {Action} from '@ngrx/store';
import {catchError, EMPTY, expand, map, Observable, switchMap} from 'rxjs';
import {SecurityActions} from '../security.actions';

export type LoadAllSecuritiesEffectArgs = {
  actions$: Actions
  securityApi: SecurityApi
};

const pageSize: number = 100;

export function loadAllSecurities(effectArgs: LoadAllSecuritiesEffectArgs): Observable<Action> {
  const {actions$, securityApi} = effectArgs;
  return actions$.pipe(
    ofType(SecurityActions.loadAllSecurities),
    switchMap((): Observable<Action> => {
      return securityApi.getSecurities(0, pageSize).pipe(
        expand((page: PaginatedSecurityRead): Observable<PaginatedSecurityRead> =>
          page.currentPage < page.lastPage ? securityApi.getSecurities(page.currentPage + 1, pageSize) : EMPTY
        ),
        map((page: PaginatedSecurityRead): Action => SecurityActions.setSecurities({securities: page.items})),
        catchError((): Observable<Action> => EMPTY)
      );
    })
  );
}
