import {computed, Signal} from '@angular/core';
import {format, isValid, parse} from 'date-fns';
import {ReadableSignalStore} from '../../../../../common/types/signal-store.type';
import {TransactionType} from '../../../../../gen/api/depot-transaction';
import {TransactionImportState} from '../transaction-import.store';
import {CsvRow, RowResolution} from '../csv/csv.type';
import {lookupTransactionType} from '../csv/lookup-transaction-type';

export type SkippedRowsSummary = {
  reason: string
  count: number
};

export type TransactionTypeCount = {
  transactionType: TransactionType
  count: number
};

export type ImportPreview = {
  countsByType: TransactionTypeCount[]
  minDate: string | null
  maxDate: string | null
  skipped: SkippedRowsSummary[]
};

export function importPreview(signalStore: ReadableSignalStore<TransactionImportState>,
                              rowResolutionsSignal: Signal<RowResolution[]>): Signal<ImportPreview> {
  return computed((): ImportPreview => {
    const columnMapping = signalStore.columnMapping();
    const transactionTypeMapping = signalStore.transactionTypeMapping();
    const dateFormat: string | null = signalStore.dateFormat();
    const rows: CsvRow[] = signalStore.rows();
    const typeColumn: number | undefined = columnMapping.transactionType;
    const dateColumn: number | undefined = columnMapping.date;

    const countsByType: Map<TransactionType, number> = new Map<TransactionType, number>();
    const skippedByReason: Map<string, number> = new Map<string, number>();
    let minDate: string | null = null;
    let maxDate: string | null = null;

    for (const resolution of rowResolutionsSignal()) {
      if (resolution.status === 'unresolvable') {
        skippedByReason.set(resolution.reason, (skippedByReason.get(resolution.reason) ?? 0) + 1);
        continue;
      }

      const row: CsvRow = rows[resolution.rowIndex];

      if (typeColumn !== undefined) {
        const rawType: string = row.values[typeColumn]?.trim() ?? '';
        const mappedType: TransactionType | null | undefined = lookupTransactionType(transactionTypeMapping, rawType);
        if (mappedType != null) {
          countsByType.set(mappedType, (countsByType.get(mappedType) ?? 0) + 1);
        }
      }

      if (dateColumn !== undefined && dateFormat !== null) {
        const dateCell: string = row.values[dateColumn]?.trim() ?? '';
        if (dateCell.length > 0) {
          const parsed: Date = parse(dateCell, dateFormat, new Date());
          if (isValid(parsed)) {
            const iso: string = format(parsed, 'yyyy-MM-dd');
            if (minDate === null || iso < minDate) {
              minDate = iso;
            }
            if (maxDate === null || iso > maxDate) {
              maxDate = iso;
            }
          }
        }
      }
    }

    return {
      countsByType: Array.from(countsByType.entries())
        .map(([transactionType, count]: [TransactionType, number]): TransactionTypeCount => ({transactionType, count})),
      minDate,
      maxDate,
      skipped: Array.from(skippedByReason.entries())
        .map(([reason, count]: [string, number]): SkippedRowsSummary => ({reason, count}))
    };
  });
}
