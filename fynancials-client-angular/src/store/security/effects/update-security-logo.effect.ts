import {Actions, ofType} from '@ngrx/effects';
import {SecurityLogoApi} from '../../../gen/api/security';
import {Action, Store} from '@ngrx/store';
import {AppState} from '../../app.state';
import {catchError, map, mergeMap, Observable, of} from 'rxjs';
import {SecurityActions, UpdateSecurityLogoActionArgs} from '../security.actions';
import {getSecurity} from '../security.selector';

export type UpdateSecurityLogoEffectArgs = {
  securityLogoApi: SecurityLogoApi
  store: Store<AppState>
};

export function updateSecurityLogo(actions$: Actions, effectArgs: UpdateSecurityLogoEffectArgs): Observable<Action> {
  return actions$.pipe(
    ofType(SecurityActions.updateSecurityLogo),
    mergeMap((actionArgs: UpdateSecurityLogoActionArgs) => {
      const {
        store,
        securityLogoApi
      } = effectArgs;
      const {
        id,
        logo
      } = actionArgs;
      const hasLogo: boolean = store.selectSignal(getSecurity(id))()?._links.logo != null;

      if (logo) {
        return securityLogoApi.setLogo(id, logo).pipe(
          map((): Action => SecurityActions.updateSecurityLogoDone()),
          catchError((): Observable<Action> => of(SecurityActions.updateSecurityLogoDone()))
        );
      } else if (hasLogo) {
        return securityLogoApi.deleteLogo(id).pipe(
          map((): Action => SecurityActions.updateSecurityLogoDone()),
          catchError((): Observable<Action> => of(SecurityActions.updateSecurityLogoDone()))
        );
      }
      return of(SecurityActions.updateSecurityLogoDone());
    })
  );
}