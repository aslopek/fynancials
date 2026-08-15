import {CsvRow} from './csv.type';

export type ParsedCsv = {
  headerRawLine: string
  header: string[]
  rows: CsvRow[]
};

function splitLine(line: string, separator: string): string[] {
  const values: string[] = [];
  let current: string = '';
  let insideQuotes: boolean = false;
  for (let i: number = 0; i < line.length; i++) {
    const char: string = line[i];
    if (insideQuotes) {
      if (char === '"') {
        if (line[i + 1] === '"') {
          current += '"';
          i++;
        } else {
          insideQuotes = false;
        }
      } else {
        current += char;
      }
    } else if (char === '"') {
      insideQuotes = true;
    } else if (char === separator) {
      values.push(current);
      current = '';
    } else {
      current += char;
    }
  }
  values.push(current);
  return values;
}

export function parseCsv(raw: string, separator: string): ParsedCsv {
  const lines: string[] = raw.split(/\r\n|\r|\n/).filter((line: string): boolean => line.length > 0);
  if (lines.length === 0) {
    return {headerRawLine: '', header: [], rows: []};
  }

  const headerRawLine: string = lines[0];
  const header: string[] = splitLine(headerRawLine, separator);
  const rows: CsvRow[] = lines.slice(1).map((line: string, index: number): CsvRow => ({
    index,
    values: splitLine(line, separator),
    rawLine: line
  }));

  return {headerRawLine, header, rows};
}
