import {
  HistoricalSecurityPriceApi,
  HistoricalSecurityPriceConfig
} from '../../../gen/api/historical-security-price';
import {
  Action,
  Store
} from '@ngrx/store';
import {AppState} from '../../app.state';
import {
  SecurityActions,
  UpdateHistoricalSecurityPriceConfigActionArgs
} from '../security.actions';
import {getHistoricalSecurityPriceConfig} from '../security.selector';
import {
  concatMap,
  firstValueFrom,
  Observable
} from 'rxjs';
import {
  Actions,
  ofType
} from '@ngrx/effects';
import {DepotActions} from '../../depot/depot.actions';

export type UpdateHistoricalSecurityPriceConfigEffectArgs = {
  store: Store<AppState>
  historicalSecurityPriceApi: HistoricalSecurityPriceApi
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
    historicalSecurityPriceApi
  } = effectArgs;
  const {
    securityId,
    historicalSecurityPriceConfig
  } = actionArgs;

  const existingConfig: HistoricalSecurityPriceConfig | null = store.selectSignal(getHistoricalSecurityPriceConfig(securityId))();

  try {
    const result: HistoricalSecurityPriceConfig = await firstValueFrom(historicalSecurityPriceApi.setHistoricalPriceConfig(securityId, {
      ...historicalSecurityPriceConfig,
      version: existingConfig?.version ?? 0
    }, true));
    store.dispatch(DepotActions.reloadDepots());
    return SecurityActions.updateHistoricalSecurityPriceConfigDone({
      securityId,
      historicalSecurityPriceConfig: result
    });
  } catch {
    return SecurityActions.updateHistoricalSecurityPriceConfigDone({securityId});
  }
}