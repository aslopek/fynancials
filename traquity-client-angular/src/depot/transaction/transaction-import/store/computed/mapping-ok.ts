import {computed, Signal} from '@angular/core';
import {isValid, parse} from 'date-fns';
import {ReadableSignalStore} from '../../../../../common/types/signal-store.type';
import {TransactionImportState} from '../transaction-import.store';
import {MappableField} from '../csv/csv.type';

const requiredFields: readonly MappableField[] = ['date', 'transactionType', 'securityCountOriginal', 'grossValue'] as const;

export function mappingOk(signalStore: ReadableSignalStore<TransactionImportState>,
                          distinctTypeValuesSignal: Signal<string[]>): Signal<boolean> {
  return computed((): boolean => {
    const columnMapping = signalStore.columnMapping();
    for (const field of requiredFields) {
      if (columnMapping[field] === undefined) {
        return false;
      }
    }
    if (columnMapping.isin === undefined && columnMapping.name === undefined && columnMapping.symbol === undefined) {
      return false;
    }

    const distinctValues: string[] = distinctTypeValuesSignal();
    if (distinctValues.length === 0) {
      return false;
    }
    const transactionTypeMapping = signalStore.transactionTypeMapping();
    for (const value of distinctValues) {
      if (!Object.hasOwn(transactionTypeMapping, value)) {
        return false;
      }
    }

    const dateFormat: string | null = signalStore.dateFormat();
    if (dateFormat === null) {
      return false;
    }
    const dateColumn: number = columnMapping.date!;
    for (const row of signalStore.rows()) {
      const cell: string = row.values[dateColumn]?.trim() ?? '';
      if (cell.length > 0 && !isValid(parse(cell, dateFormat, new Date()))) {
        return false;
      }
    }

    return true;
  });
}
