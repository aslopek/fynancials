import {patchState} from '@ngrx/signals';
import {Store} from '@ngrx/store';
import {firstValueFrom} from 'rxjs';
import {format, parse} from 'date-fns';
import {WritableSignalStore} from '../../../../../common/types/signal-store.type';
import {AppState} from '../../../../../store/app.state';
import {SecurityActions} from '../../../../../store/security/security.actions';
import {DepotActions} from '../../../../../store/depot/depot.actions';
import {selectedDepotIds} from '../../../../../store/depot/depot.selector';
import {SecurityApi, SecurityCreate, SecurityRead} from '../../../../../gen/api/security';
import {TransactionApi, TransactionCreate, TransactionRead, TransactionType} from '../../../../../gen/api/depot-transaction';
import {ReadableTransactionPageStore} from '../../../transaction-store/transaction-page.store';
import {TransactionImportComputed, TransactionImportState} from '../transaction-import.store';
import {ColumnMapping, CsvRow, RowResolution} from '../csv/csv.type';
import {parseDecimal} from '../csv/parse-decimal';
import {lookupTransactionType} from '../csv/lookup-transaction-type';

const delayBetweenRequestsMs: number = 20;

type ImportableResolution = Extract<RowResolution, { status: 'known' } | { status: 'unknown' }>;

function isImportable(resolution: RowResolution): resolution is ImportableResolution {
  return resolution.status !== 'unresolvable';
}

function normalizeTime(value: string): string | undefined {
  const trimmed: string = value.trim();
  if (trimmed.length === 0) {
    return undefined;
  }
  return /^\d{2}:\d{2}$/.test(trimmed) ? `${trimmed}:00` : trimmed;
}

function buildTransactionCreate(row: CsvRow, columnMapping: ColumnMapping, securityId: number, dateFormat: string,
                                decimalSeparator: ',' | '.',
                                transactionTypeMapping: { [csvValue: string]: TransactionType | null }): TransactionCreate {
  const dateCell: string = row.values[columnMapping.date!].trim();
  const date: string = format(parse(dateCell, dateFormat, new Date()), 'yyyy-MM-dd');

  const typeCell: string = row.values[columnMapping.transactionType!].trim();
  const transactionType: TransactionType = lookupTransactionType(transactionTypeMapping, typeCell)!;

  const securityCountOriginal: number = parseDecimal(row.values[columnMapping.securityCountOriginal!], decimalSeparator)!;
  const grossValue: number = parseDecimal(row.values[columnMapping.grossValue!], decimalSeparator)!;

  const time: string | undefined = columnMapping.time !== undefined ? normalizeTime(row.values[columnMapping.time] ?? '') : undefined;
  const tax: number | undefined = columnMapping.tax !== undefined
    ? parseDecimal(row.values[columnMapping.tax] ?? '', decimalSeparator) ?? undefined
    : undefined;
  const fee: number | undefined = columnMapping.fee !== undefined
    ? parseDecimal(row.values[columnMapping.fee] ?? '', decimalSeparator) ?? undefined
    : undefined;

  return {date, time, securityId, transactionType, securityCountOriginal, grossValue, tax, fee};
}

function finish(signalStore: WritableSignalStore<TransactionImportState, TransactionImportComputed>,
                globalStore: Store<AppState>,
                transactionPageStore: ReadableTransactionPageStore): void {
  patchState(signalStore, {phase: 'done'});
  globalStore.dispatch(DepotActions.reloadDepots());
  transactionPageStore.reloadFirstPage();
}

export async function runImport(signalStore: WritableSignalStore<TransactionImportState, TransactionImportComputed>,
                                globalStore: Store<AppState>,
                                securityApi: SecurityApi,
                                transactionApi: TransactionApi,
                                transactionPageStore: ReadableTransactionPageStore): Promise<void> {
  const rowResolutionList: RowResolution[] = signalStore.rowResolutions();
  const unknownSecurityList: SecurityCreate[] = signalStore.unknownSecurities();
  const importableRows: ImportableResolution[] = rowResolutionList.filter(isImportable);
  const initialFailedRowIndices: number[] = rowResolutionList
    .filter((resolution: RowResolution): boolean => resolution.status === 'unresolvable')
    .map((resolution: RowResolution): number => resolution.rowIndex);

  patchState(signalStore, {
    phase: 'importing',
    requestsSent: 0,
    requestsTotal: unknownSecurityList.length + importableRows.length,
    securitiesAttempted: 0,
    securitiesCreated: 0,
    transactionsAttempted: 0,
    transactionsCreated: 0,
    failedRowIndices: initialFailedRowIndices,
    cancelled: false
  });

  const securityIdByIsin: Map<string, number> = new Map<string, number>();

  for (const security of unknownSecurityList) {
    if (signalStore.cancelled()) {
      finish(signalStore, globalStore, transactionPageStore);
      return;
    }

    let created: SecurityRead | undefined;
    try {
      created = await firstValueFrom(securityApi.createSecurity(security));
    } catch (ignored) {
      created = undefined;
    }

    if (created !== undefined) {
      globalStore.dispatch(SecurityActions.setSecurities({securities: [created]}));
      securityIdByIsin.set(security.isin, created.id);
    }

    patchState(signalStore, {
      requestsSent: signalStore.requestsSent() + 1,
      securitiesAttempted: signalStore.securitiesAttempted() + 1,
      securitiesCreated: signalStore.securitiesCreated() + (created !== undefined ? 1 : 0)
    });
  }

  const rows: CsvRow[] = signalStore.rows();
  const columnMapping: ColumnMapping = signalStore.columnMapping();
  const decimalSeparator: ',' | '.' = signalStore.decimalSeparator();
  const dateFormat: string = signalStore.dateFormat()!;
  const transactionTypeMapping: { [csvValue: string]: TransactionType | null } = signalStore.transactionTypeMapping();
  const depotId: number = globalStore.selectSignal(selectedDepotIds)()[0];

  for (const resolution of importableRows) {
    if (signalStore.cancelled()) {
      finish(signalStore, globalStore, transactionPageStore);
      return;
    }

    const securityId: number | undefined = resolution.status === 'known' ? resolution.securityId : securityIdByIsin.get(resolution.isin);
    if (securityId === undefined) {
      patchState(signalStore, {failedRowIndices: [...signalStore.failedRowIndices(), resolution.rowIndex]});
      continue;
    }

    const row: CsvRow = rows[resolution.rowIndex];
    const transactionCreate: TransactionCreate =
      buildTransactionCreate(row, columnMapping, securityId, dateFormat, decimalSeparator, transactionTypeMapping);

    let created: TransactionRead | undefined;
    try {
      created = await firstValueFrom(transactionApi.createTransaction(depotId, transactionCreate));
    } catch (ignored) {
      created = undefined;
    }

    patchState(signalStore, {
      requestsSent: signalStore.requestsSent() + 1,
      transactionsAttempted: signalStore.transactionsAttempted() + 1,
      transactionsCreated: signalStore.transactionsCreated() + (created !== undefined ? 1 : 0),
      failedRowIndices: created === undefined ? [...signalStore.failedRowIndices(), resolution.rowIndex] : signalStore.failedRowIndices()
    });

    await new Promise<void>((resolve): void => {
      setTimeout(resolve, delayBetweenRequestsMs);
    });
  }

  finish(signalStore, globalStore, transactionPageStore);
}
