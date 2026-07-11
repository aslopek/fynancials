import {
  HistoricalSecurityPriceConfigs,
  SecurityState
} from '../security.state';

export type getHistoricalSecurityPriceConfigState = Pick<SecurityState, 'historicalSecurityPriceConfigs'>;

export function getHistoricalSecurityPriceConfigsSelector(state: getHistoricalSecurityPriceConfigState): HistoricalSecurityPriceConfigs {
  return state.historicalSecurityPriceConfigs;
}
