import {HistoricalSecurityPriceConfigApi, HistoricalSecurityPriceConfigRead} from '../../../gen/api/historical-security-price';
import {Action, Store} from '@ngrx/store';
import {AppState} from '../../app.state';
import {LoadHistoricalSecurityPriceConfigActionArgs, SecurityActions} from '../security.actions';
import {getHistoricalSecurityPriceConfig} from '../security.selector';
import {firstValueFrom, mergeMap, Observable} from 'rxjs';
import {Actions, ofType} from '@ngrx/effects';

export type LoadHistoricalSecurityPriceConfigEffectArgs = {
  historicalSecurityPriceConfigApi: HistoricalSecurityPriceConfigApi
  store: Store<AppState>
};

export function loadHistoricalSecurityPriceConfig(actions$: Actions, effectArgs: LoadHistoricalSecurityPriceConfigEffectArgs): Observable<Action> {
  return actions$.pipe(
    ofType(SecurityActions.loadHistoricalSecurityPriceConfig),
    mergeMap(async (actionArgs: LoadHistoricalSecurityPriceConfigActionArgs): Promise<Action> =>
      loadHistoricalSecurityPriceConfigHelper(effectArgs, actionArgs)
    )
  );
}

async function loadHistoricalSecurityPriceConfigHelper(effectArgs: LoadHistoricalSecurityPriceConfigEffectArgs,
                                                       actionArgs: LoadHistoricalSecurityPriceConfigActionArgs): Promise<Action> {
  const {
    historicalSecurityPriceConfigApi,
    store
  } = effectArgs;
  const {securityId} = actionArgs;
  const config: HistoricalSecurityPriceConfigRead | null = store.selectSignal(getHistoricalSecurityPriceConfig(securityId))();

  if (config !== null) {
    return SecurityActions.loadHistoricalSecurityPriceConfigDone({securityId});
  }

  try {
    const historicalSecurityPriceConfig: HistoricalSecurityPriceConfigRead
      = await firstValueFrom(historicalSecurityPriceConfigApi.getHistoricalSecurityPriceConfig(securityId));
    return SecurityActions.loadHistoricalSecurityPriceConfigDone({
      securityId,
      historicalSecurityPriceConfig
    });
  } catch {
    return SecurityActions.loadHistoricalSecurityPriceConfigDone({securityId});
  }
}
