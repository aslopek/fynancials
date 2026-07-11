import {isValid, parse} from 'date-fns';

const candidateFormats: readonly string[] = ['yyyy-MM-dd', 'dd.MM.yyyy', 'dd/MM/yyyy', 'MM/dd/yyyy', 'd.M.yyyy', 'yyyy/MM/dd'] as const;

export function detectDateFormat(values: string[]): string | null {
  const nonEmptyValues: string[] = values.map((value: string): string => value.trim())
    .filter((value: string): boolean => value.length > 0);
  if (nonEmptyValues.length === 0) {
    return null;
  }

  for (const candidate of candidateFormats) {
    const allValid: boolean = nonEmptyValues.every((value: string): boolean => isValid(parse(value, candidate, new Date())));
    if (allValid) {
      return candidate;
    }
  }
  return null;
}
