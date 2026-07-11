function escapeForRegExp(char: string): string {
  return char.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
}

export function parseDecimal(value: string, decimalSeparator: ',' | '.'): number | null {
  const thousandsSeparator: ',' | '.' = decimalSeparator === ',' ? '.' : ',';
  const trimmed: string = value.trim();
  if (trimmed.length === 0) {
    return null;
  }

  const decimalSeparatorPattern: string = escapeForRegExp(decimalSeparator);
  const thousandsSeparatorPattern: string = escapeForRegExp(thousandsSeparator);
  const numberPattern: RegExp = new RegExp(
    `^[+-]?(?:\\d+|\\d{1,3}(?:${thousandsSeparatorPattern}\\d{3})+)(?:${decimalSeparatorPattern}\\d+)?$`
  );
  if (!numberPattern.test(trimmed)) {
    return null;
  }

  const normalized: string = trimmed.split(thousandsSeparator).join('').replace(decimalSeparator, '.');
  const parsed: number = Number(normalized);
  return Number.isFinite(parsed) ? parsed : null;
}
