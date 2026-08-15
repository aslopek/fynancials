import {SecurityRead} from '../../gen/api/security';
import {HistoricalSecurityPriceConfig} from '../../gen/api/historical-security-price';
import {DataSourceWithId} from "../../settings/data-source/data-source.type";

export type SecuritiesById = { [id: number]: SecurityRead };
export type HistoricalSecurityPriceConfigs = { [id: number]: HistoricalSecurityPriceConfig };

export type SecurityState = {
  securities: SecuritiesById
  historicalSecurityPriceConfigs: HistoricalSecurityPriceConfigs
  historicalSecurityPriceDataSources: DataSourceWithId[]
};
