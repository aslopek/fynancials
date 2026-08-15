import {SecurityState} from '../security.state';
import {HistoricalSecurityPriceConfig} from '../../../gen/api/historical-security-price';

export type GetHistoricalSecurityPriceConfigState = Pick<SecurityState, 'historicalSecurityPriceConfigs'>;

export function getHistoricalSecurityPriceConfigSelector(state: GetHistoricalSecurityPriceConfigState, securityId: number): HistoricalSecurityPriceConfig | null {
  return state.historicalSecurityPriceConfigs[securityId] ?? null;
}
