import {HistoricalSecurityPriceConfigApi, HistoricalSecurityPriceConfigRead} from '../../../gen/api/historical-security-price';
import {Action, Store} from '@ngrx/store';
import {AppState} from '../../app.state';
import {SecurityActions, UpdateHistoricalSecurityPriceConfigActionArgs} from '../security.actions';
import {getHistoricalSecurityPriceConfig} from '../security.selector';
import {concatMap, firstValueFrom, Observable} from 'rxjs';
import {Actions, ofType} from '@ngrx/effects';

export type UpdateHistoricalSecurityPriceConfigEffectArgs = {
  store: Store<AppState>
  historicalSecurityPriceConfigApi: HistoricalSecurityPriceConfigApi
};

export function updateHistoricalSecurityPriceConfigEffect(actions$: Actions,
                                                          effectArgs: UpdateHistoricalSecurityPriceConfigEffectArgs): Observable<Action> {
  return actions$.pipe(
    ofType(SecurityActions.updateHistoricalSecurityPriceConfig),
    concatMap(async (actionArgs: UpdateHistoricalSecurityPriceConfigActionArgs): Promise<Action> =>
      updateHistoricalSecurityPriceConfigEffectHelper(effectArgs, actionArgs)
    )
  );
}

async function updateHistoricalSecurityPriceConfigEffectHelper(effectArgs: UpdateHistoricalSecurityPriceConfigEffectArgs,
                                                               actionArgs: UpdateHistoricalSecurityPriceConfigActionArgs): Promise<Action> {
  const {
    store,
    historicalSecurityPriceConfigApi
  } = effectArgs;
  const {
    securityId,
    historicalSecurityPriceConfig
  } = actionArgs;

  const existingConfig: HistoricalSecurityPriceConfigRead | null = store.selectSignal(getHistoricalSecurityPriceConfig(securityId))();

  const request: Observable<HistoricalSecurityPriceConfigRead> = existingConfig === null
    ? historicalSecurityPriceConfigApi.createHistoricalSecurityPriceConfig(securityId, historicalSecurityPriceConfig)
    : historicalSecurityPriceConfigApi.updateHistoricalSecurityPriceConfig(securityId, {
      ...historicalSecurityPriceConfig,
      version: existingConfig.version
    }, true);

  try {
    const result: HistoricalSecurityPriceConfigRead = await firstValueFrom(request);
    return SecurityActions.updateHistoricalSecurityPriceConfigDone({
      securityId,
      historicalSecurityPriceConfig: result
    });
  } catch {
    return SecurityActions.updateHistoricalSecurityPriceConfigDone({securityId});
  }
}