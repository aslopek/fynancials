import {CsvRow} from './csv.type';

export function buildFailedCsv(headerRawLine: string, failedRows: CsvRow[]): string {
  return [headerRawLine, ...failedRows.map((row: CsvRow): string => row.rawLine)].join('\n');
}
