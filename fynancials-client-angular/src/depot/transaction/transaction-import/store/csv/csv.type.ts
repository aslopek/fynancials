export type CsvRow = {
  index: number
  values: string[]
  rawLine: string
};

export type MappableField =
  | 'date'
  | 'time'
  | 'isin'
  | 'name'
  | 'symbol'
  | 'wkn'
  | 'transactionType'
  | 'securityCountOriginal'
  | 'grossValue'
  | 'tax'
  | 'fee';

export type ColumnMapping = Partial<Record<MappableField, number>>;

export type ImportPhase = 'configuring' | 'importing' | 'done';

export type RowResolution =
  | { rowIndex: number, status: 'known', securityId: number }
  | { rowIndex: number, status: 'unknown', isin: string }
  | { rowIndex: number, status: 'unresolvable', reason: string };
