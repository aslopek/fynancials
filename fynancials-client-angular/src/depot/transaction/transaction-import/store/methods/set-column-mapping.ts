import {patchState} from '@ngrx/signals';
import {WritableSignalStore} from '../../../../../common/types/signal-store.type';
import {TransactionType} from '../../../../../gen/api/depot-transaction';
import {TransactionImportComputed, TransactionImportState} from '../transaction-import.store';
import {ColumnMapping, MappableField} from '../csv/csv.type';
import {detectDateFormat} from '../csv/detect-date-format';

export function setColumnMapping(signalStore: WritableSignalStore<TransactionImportState, TransactionImportComputed>,
                                 field: MappableField, columnIndex: number | null): void {
  const columnMapping: ColumnMapping = {...signalStore.columnMapping()};
  if (columnIndex === null) {
    delete columnMapping[field];
  } else {
    columnMapping[field] = columnIndex;
  }

  let dateFormat: string | null = signalStore.dateFormat();
  if (field === 'date') {
    if (columnIndex === null) {
      dateFormat = null;
    } else {
      const values: string[] = signalStore.rows().map((row): string => row.values[columnIndex] ?? '');
      dateFormat = detectDateFormat(values);
    }
  }

  const transactionTypeMapping: { [csvValue: string]: TransactionType | null } = field === 'transactionType'
    ? {}
    : signalStore.transactionTypeMapping();

  patchState(signalStore, {columnMapping, dateFormat, transactionTypeMapping});
}
