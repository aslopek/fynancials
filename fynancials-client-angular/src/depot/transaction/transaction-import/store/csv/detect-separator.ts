const candidateSeparators: readonly string[] = [';', ',', '\t', '|'] as const;
const maxSampledLines: number = 20;

function countColumnsOutsideQuotes(line: string, separator: string): number {
  let count: number = 1;
  let insideQuotes: boolean = false;
  for (let i: number = 0; i < line.length; i++) {
    const char: string = line[i];
    if (char === '"') {
      insideQuotes = !insideQuotes;
    } else if (char === separator && !insideQuotes) {
      count++;
    }
  }
  return count;
}

export function detectSeparator(raw: string): string | null {
  const lines: string[] = raw.split(/\r\n|\r|\n/)
    .filter((line: string): boolean => line.trim().length > 0)
    .slice(0, maxSampledLines);
  if (lines.length === 0) {
    return null;
  }

  for (const separator of candidateSeparators) {
    const columnCount: number = countColumnsOutsideQuotes(lines[0], separator);
    if (columnCount <= 1) {
      continue;
    }
    const consistent: boolean = lines.every((line: string): boolean => countColumnsOutsideQuotes(line, separator) === columnCount);
    if (consistent) {
      return separator;
    }
  }
  return null;
}
