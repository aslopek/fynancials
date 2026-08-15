import {computed, Signal} from '@angular/core';
import {isValid, parse} from 'date-fns';
import {ReadableSignalStore} from '../../../../../common/types/signal-store.type';
import {TransactionImportState} from '../transaction-import.store';
import {CsvRow, RowResolution} from '../csv/csv.type';
import {parseDecimal} from '../csv/parse-decimal';
import {lookupTransactionType} from '../csv/lookup-transaction-type';
import {SecuritiesLookup} from './securities-lookup';

export function rowResolutions(signalStore: ReadableSignalStore<TransactionImportState>,
                               securitiesLookupSignal: Signal<SecuritiesLookup>): Signal<RowResolution[]> {
  return computed((): RowResolution[] => {
    const columnMapping = signalStore.columnMapping();
    const transactionTypeMapping = signalStore.transactionTypeMapping();
    const dateFormat: string | null = signalStore.dateFormat();
    const decimalSeparator: ',' | '.' = signalStore.decimalSeparator();
    const lookup: SecuritiesLookup = securitiesLookupSignal();

    return signalStore.rows().map((row: CsvRow): RowResolution => {
      const typeColumn: number | undefined = columnMapping.transactionType;
      const rawType: string = typeColumn !== undefined ? (row.values[typeColumn]?.trim() ?? '') : '';
      const mappedType = lookupTransactionType(transactionTypeMapping, rawType);
      if (mappedType === null) {
        return {rowIndex: row.index, status: 'unresolvable', reason: 'Type mapped to skip'};
      }
      if (mappedType === undefined) {
        return {rowIndex: row.index, status: 'unresolvable', reason: 'Type not mapped'};
      }

      const dateColumn: number | undefined = columnMapping.date;
      const dateCell: string = dateColumn !== undefined ? (row.values[dateColumn]?.trim() ?? '') : '';
      if (dateCell.length === 0 || dateFormat === null || !isValid(parse(dateCell, dateFormat, new Date()))) {
        return {rowIndex: row.index, status: 'unresolvable', reason: 'Invalid date'};
      }

      const countColumn: number | undefined = columnMapping.securityCountOriginal;
      const grossValueColumn: number | undefined = columnMapping.grossValue;
      const countCell: string = countColumn !== undefined ? (row.values[countColumn] ?? '') : '';
      const grossValueCell: string = grossValueColumn !== undefined ? (row.values[grossValueColumn] ?? '') : '';
      if (parseDecimal(countCell, decimalSeparator) === null || parseDecimal(grossValueCell, decimalSeparator) === null) {
        return {rowIndex: row.index, status: 'unresolvable', reason: 'Invalid number'};
      }

      const isinColumn: number | undefined = columnMapping.isin;
      const isinCell: string = isinColumn !== undefined ? (row.values[isinColumn]?.trim() ?? '') : '';
      if (isinCell.length > 0) {
        const knownByIsin = lookup.byIsin.get(isinCell.toUpperCase());
        if (knownByIsin !== undefined) {
          return {rowIndex: row.index, status: 'known', securityId: knownByIsin.id};
        }
      }

      const nameColumn: number | undefined = columnMapping.name;
      const nameCell: string = nameColumn !== undefined ? (row.values[nameColumn]?.trim() ?? '') : '';
      if (nameCell.length > 0) {
        const knownByName = lookup.byName.get(nameCell.toLowerCase());
        if (knownByName !== undefined) {
          return {rowIndex: row.index, status: 'known', securityId: knownByName.id};
        }
      }

      const symbolColumn: number | undefined = columnMapping.symbol;
      const symbolCell: string = symbolColumn !== undefined ? (row.values[symbolColumn]?.trim() ?? '') : '';
      if (symbolCell.length > 0) {
        const knownBySymbol = lookup.bySymbol.get(symbolCell.toUpperCase());
        if (knownBySymbol !== undefined) {
          return {rowIndex: row.index, status: 'known', securityId: knownBySymbol.id};
        }
      }

      if (isinCell.length > 0 && nameCell.length > 0) {
        return {rowIndex: row.index, status: 'unknown', isin: isinCell.toUpperCase()};
      }
      return {rowIndex: row.index, status: 'unresolvable', reason: 'Unknown security requires ISIN and name'};
    });
  });
}
