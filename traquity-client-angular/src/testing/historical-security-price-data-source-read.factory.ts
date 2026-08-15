import {HistoricalSecurityPriceDataSourceRead} from '../gen/api/historical-security-price';

export function historicalSecurityPriceDataSourceReadFactory(
  overrides?: Partial<HistoricalSecurityPriceDataSourceRead>
): HistoricalSecurityPriceDataSourceRead {
  return {
    id: 1,
    version: 0,
    name: 'Data Source',
    urlPatterns: [],
    requestHeaders: [],
    jsonPathDate: '$.date',
    dateFormat: {format: 'TIMESTAMP_SECONDS'},
    jsonPathValue: '$.value',
    currencyMappings: [],
    marketCloseTimes: [],
    ...overrides
  };
}
