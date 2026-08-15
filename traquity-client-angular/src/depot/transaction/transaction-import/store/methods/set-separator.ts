import {patchState} from '@ngrx/signals';
import {WritableSignalStore} from '../../../../../common/types/signal-store.type';
import {TransactionImportComputed, TransactionImportState} from '../transaction-import.store';
import {parseCsv, ParsedCsv} from '../csv/parse-csv';

export function setSeparator(signalStore: WritableSignalStore<TransactionImportState, TransactionImportComputed>, separator: string): void {
  const rawText: string | null = signalStore.rawText();
  if (rawText === null) {
    return;
  }

  const parsed: ParsedCsv = parseCsv(rawText, separator);
  patchState(signalStore, {
    separator,
    headerRawLine: parsed.headerRawLine,
    header: parsed.header,
    rows: parsed.rows,
    columnMapping: {},
    transactionTypeMapping: {},
    dateFormat: null
  });
}
