import {HistoricalSecurityPriceConfig} from '../gen/api/historical-security-price';

export function historicalSecurityPriceConfigFactory(overrides?: Partial<HistoricalSecurityPriceConfig>): HistoricalSecurityPriceConfig {
  return {
    dataSourceId: 1,
    externalSecurityId: 'AAPL',
    isActive: true,
    version: 0,
    ...overrides
  };
}
