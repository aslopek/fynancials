import {
  Action,
  Store
} from '@ngrx/store';
import {
  SecurityApi,
  SecurityRead
} from '../../../gen/api/security';
import {
  firstValueFrom,
  mergeMap,
  Observable
} from 'rxjs';
import {
  LoadSecurityActionArgs,
  SecurityActions
} from '../security.actions';
import {
  Actions,
  ofType
} from '@ngrx/effects';
import {AppState} from '../../app.state';
import {getSecurity} from '../security.selector';

export type LoadSecurityEffectArgs = {
  securityApi: SecurityApi
  store: Store<AppState>
};

export function loadSecurity(actions$: Actions, effectArgs: LoadSecurityEffectArgs): Observable<Action> {
  return actions$.pipe(
    ofType(SecurityActions.loadSecurity),
    mergeMap(async (actionArgs: LoadSecurityActionArgs): Promise<Action> => loadSecurityHelper(effectArgs, actionArgs))
  );
}

async function loadSecurityHelper(effectArgs: LoadSecurityEffectArgs, actionArgs: LoadSecurityActionArgs): Promise<Action> {
  const {
    securityApi,
    store
  } = effectArgs;

  const {securityId} = actionArgs;
  const security: SecurityRead | null = store.selectSignal(getSecurity(securityId))();
  if (security != null) {
    return SecurityActions.loadSecurityError(actionArgs);
  }

  try {
    const security: SecurityRead | null = await firstValueFrom(securityApi.getSecurity(securityId));
    if (security === null) {
      return SecurityActions.loadSecurityError(actionArgs);
    }
    return SecurityActions.loadSecuritySuccess({security});
  } catch {
    return SecurityActions.loadSecurityError(actionArgs);
  }
}
