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
  LoadHistoricalSecurityPriceConfigActionArgs,
  SecurityActions
} from '../security.actions';
import {getHistoricalSecurityPriceConfig} from '../security.selector';
import {
  firstValueFrom,
  mergeMap,
  Observable
} from 'rxjs';
import {
  Actions,
  ofType
} from '@ngrx/effects';

export type LoadHistoricalSecurityPriceConfigEffectArgs = {
  historicalSecurityPriceApi: HistoricalSecurityPriceApi
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
    historicalSecurityPriceApi,
    store
  } = effectArgs;
  const {securityId} = actionArgs;
  const config: HistoricalSecurityPriceConfig | null = store.selectSignal(getHistoricalSecurityPriceConfig(securityId))();

  if (config !== null) {
    return SecurityActions.loadHistoricalSecurityPriceConfigDone({securityId});
  }

  try {
    const historicalSecurityPriceConfig: HistoricalSecurityPriceConfig
      = await firstValueFrom(historicalSecurityPriceApi.getHistoricalPriceConfig(securityId));
    return SecurityActions.loadHistoricalSecurityPriceConfigDone({
      securityId,
      historicalSecurityPriceConfig
    });
  } catch {
    return SecurityActions.loadHistoricalSecurityPriceConfigDone({securityId});
  }
}
