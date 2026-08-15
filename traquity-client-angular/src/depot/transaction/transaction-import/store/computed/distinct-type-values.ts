import {computed, Signal} from '@angular/core';
import {ReadableSignalStore} from '../../../../../common/types/signal-store.type';
import {TransactionImportState} from '../transaction-import.store';

export function distinctTypeValues(signalStore: ReadableSignalStore<TransactionImportState>): Signal<string[]> {
  return computed((): string[] => {
    const column: number | undefined = signalStore.columnMapping().transactionType;
    if (column === undefined) {
      return [];
    }

    const values: Set<string> = new Set<string>();
    for (const row of signalStore.rows()) {
      const value: string = row.values[column]?.trim() ?? '';
      if (value.length > 0) {
        values.add(value);
      }
    }
    return Array.from(values).sort();
  });
}
