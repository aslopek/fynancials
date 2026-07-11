import {computed, Signal} from '@angular/core';
import {ReadableSignalStore} from '../../../../../common/types/signal-store.type';
import {SecurityCreate, SecurityType} from '../../../../../gen/api/security';
import {TransactionImportState} from '../transaction-import.store';
import {CsvRow, RowResolution} from '../csv/csv.type';

export function unknownSecurities(signalStore: ReadableSignalStore<TransactionImportState>,
                                  rowResolutionsSignal: Signal<RowResolution[]>): Signal<SecurityCreate[]> {
  return computed((): SecurityCreate[] => {
    const columnMapping = signalStore.columnMapping();
    const rows: CsvRow[] = signalStore.rows();
    const byIsin: Map<string, SecurityCreate> = new Map<string, SecurityCreate>();

    for (const resolution of rowResolutionsSignal()) {
      if (resolution.status !== 'unknown' || byIsin.has(resolution.isin)) {
        continue;
      }

      const row: CsvRow = rows[resolution.rowIndex];
      const name: string = columnMapping.name !== undefined ? (row.values[columnMapping.name]?.trim() ?? '') : '';
      const symbol: string = columnMapping.symbol !== undefined ? (row.values[columnMapping.symbol]?.trim() ?? '') : '';
      const wkn: string = columnMapping.wkn !== undefined ? (row.values[columnMapping.wkn]?.trim() ?? '') : '';

      const security: SecurityCreate = {
        isin: resolution.isin,
        name,
        symbols: symbol.length > 0 ? [symbol] : [],
        securityType: SecurityType.STOCK
      };
      if (wkn.length > 0) {
        security.wkn = wkn;
      }
      byIsin.set(resolution.isin, security);
    }

    return Array.from(byIsin.values());
  });
}
