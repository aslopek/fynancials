import {SecurityState} from '../security.state';
import {HistoricalSecurityPriceConfig} from '../../../gen/api/historical-security-price';

export function overwriteHistoricalSecurityPriceConfig(state: SecurityState, securityId: number, config?: HistoricalSecurityPriceConfig): SecurityState {
  if (config === undefined) {
    return state;
  }

  return {
    ...state,
    historicalSecurityPriceConfigs: {
      ...state.historicalSecurityPriceConfigs,
      [securityId]: config
    }
  };
}
