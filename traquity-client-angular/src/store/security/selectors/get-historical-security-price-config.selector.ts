import {SecurityState} from '../security.state';
import {HistoricalSecurityPriceConfigRead} from '../../../gen/api/historical-security-price';

export type GetHistoricalSecurityPriceConfigState = Pick<SecurityState, 'historicalSecurityPriceConfigs'>;

export function getHistoricalSecurityPriceConfigSelector(state: GetHistoricalSecurityPriceConfigState, securityId: number): HistoricalSecurityPriceConfigRead | null {
  return state.historicalSecurityPriceConfigs[securityId] ?? null;
}
