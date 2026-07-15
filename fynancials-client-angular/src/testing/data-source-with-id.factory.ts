import {DataSourceWithId, MultiUrlDataSource} from '../settings/data-source/data-source.type';

type DataSourceWithIdOverrides = Partial<MultiUrlDataSource & { id: number, version: number }>;

export function dataSourceWithIdFactory(overrides?: DataSourceWithIdOverrides): DataSourceWithId {
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
    ...overrides
  };
}
