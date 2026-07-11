import {patchState} from '@ngrx/signals';
import {WritableSignalStore} from '../../../../../common/types/signal-store.type';
import {TransactionImportComputed, TransactionImportState} from '../transaction-import.store';
import {CsvRow} from '../csv/csv.type';
import {detectSeparator} from '../csv/detect-separator';
import {parseCsv, ParsedCsv} from '../csv/parse-csv';

export async function setFile(signalStore: WritableSignalStore<TransactionImportState, TransactionImportComputed>, file: File): Promise<void> {
  const rawText: string = await file.text();
  const separator: string | null = detectSeparator(rawText);

  let headerRawLine: string = '';
  let header: string[] = [];
  let rows: CsvRow[] = [];
  if (separator !== null) {
    const parsed: ParsedCsv = parseCsv(rawText, separator);
    headerRawLine = parsed.headerRawLine;
    header = parsed.header;
    rows = parsed.rows;
  }

  patchState(signalStore, {
    fileName: file.name,
    rawText,
    separator,
    headerRawLine,
    header,
    rows,
    columnMapping: {},
    transactionTypeMapping: {},
    dateFormat: null,
    decimalSeparator: ',',
    phase: 'configuring',
    requestsSent: 0,
    requestsTotal: 0,
    securitiesAttempted: 0,
    securitiesCreated: 0,
    transactionsAttempted: 0,
    transactionsCreated: 0,
    failedRowIndices: [],
    cancelled: false
  });
}
