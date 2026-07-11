export type DataSourceVariant = 'historical-security-price' | 'dividend-announcement';

export type CurrencyMapping = {
  currencyKey: string
  mappedCurrencyCode: string
  multiplier?: number
};

export type DateConfiguration = {
  format: 'TIMESTAMP_SECONDS' | 'TIMESTAMP_MILLISECONDS' | 'CUSTOM_STRING'
  customPattern?: string
};

export type RequestHeader = {
  headerName: string
  headerValue: string
};

export type UrlPattern = {
  timespanInDays: number
  urlPattern: string
};

export type ZonedTime = {
  time: string
  timeZone: string
}

export type DataSource = {
  name: string
  requestHeaders: RequestHeader[]
  jsonPathDate: string
  dateFormat: DateConfiguration
  jsonPathValue: string
  jsonPathCurrency?: string
  regexCurrency?: string
  regexCurrencyGroup?: number
  currencyMappings: CurrencyMapping[]
  marketCloseTimes?: ZonedTime[]
};

export type SingleUrlDataSource = DataSource & {
  urlPattern: string
  urlPatterns?: never
};

export type MultiUrlDataSource = DataSource & {
  urlPattern?: never
  urlPatterns: UrlPattern[]
};

export type AnyDataSource = SingleUrlDataSource | MultiUrlDataSource;

export type DataSourceWithId = AnyDataSource & {
  id: number
  version: number
};
