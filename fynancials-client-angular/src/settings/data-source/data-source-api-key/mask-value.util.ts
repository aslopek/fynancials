import {UrlPattern} from "../data-source.type";

const MASKED_VALUE_PATTERN: RegExp = /#mask\(((?:[^()]+|\([^()]*\))*)\)/;

export function extractMaskedValue(urlPattern: string): string | null {
  const match: RegExpMatchArray | null = urlPattern.match(MASKED_VALUE_PATTERN);
  return match ? match[1] : null;
}

export function replaceMaskedValue(urlPattern: string, newValue: string): string {
  return urlPattern.replace(MASKED_VALUE_PATTERN, `#mask(${newValue})`);
}

export function findMaskedValue(urlPatterns: UrlPattern[]): string | null {
  for (const pattern of urlPatterns) {
    const value: string | null = extractMaskedValue(pattern.urlPattern);
    if (value !== null) {
      return value;
    }
  }
  return null;
}
