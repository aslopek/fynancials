import {
  Actions,
  ofType
} from '@ngrx/effects';
import {
  Action,
  Store
} from '@ngrx/store';
import {
  concatMap,
  firstValueFrom,
  Observable
} from 'rxjs';
import {
  SecurityApi,
  SecurityRead,
  SecurityUpdate
} from '../../../gen/api/security';
import {
  SecurityActions,
  UpdateSecurityActionArgs
} from '../security.actions';
import {getSecurity} from '../security.selector';
import {AppState} from '../../app.state';

export type UpdateSecurityEffectArgs = {
  store: Store<AppState>
  securityApi: SecurityApi
};

export function updateSecurity(actions$: Actions, effectArgs: UpdateSecurityEffectArgs): Observable<Action> {
  return actions$.pipe(
    ofType(SecurityActions.updateSecurity),
    concatMap(async (actionArgs: UpdateSecurityActionArgs): Promise<Action> => updateSecurityHelper(effectArgs, actionArgs))
  );
}

async function updateSecurityHelper(effectArgs: UpdateSecurityEffectArgs, actionArgs: UpdateSecurityActionArgs): Promise<Action> {
  const {
    store,
    securityApi
  } = effectArgs;

  const {
    id,
    values
  } = actionArgs;

  const security: SecurityRead | null = await firstValueFrom(store.select(getSecurity(id)));
  if (security === null) {
    return SecurityActions.updateSecurityError;
  }

  const securityUpdate: SecurityUpdate = {
    version: security.version,
    ...values
  };

  try {
    const updatedSecurity: SecurityRead = await firstValueFrom(securityApi.updateSecurity(id, securityUpdate));
    return SecurityActions.updateSecuritySuccess({security: updatedSecurity});
  } catch {
    return SecurityActions.updateSecurityError();
  }
}
