import {HistoricalSecurityPriceConfigRead} from '../gen/api/historical-security-price';

export function historicalSecurityPriceConfigFactory(overrides?: Partial<HistoricalSecurityPriceConfigRead>): HistoricalSecurityPriceConfigRead {
  return {
    securityId: 1,
    dataSourceId: 1,
    externalSecurityId: 'AAPL',
    isActive: true,
    version: 0,
    ...overrides
  };
}
