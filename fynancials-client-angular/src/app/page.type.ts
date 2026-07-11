export type Page = 'securities'
  | 'depots'
  | 'dividends'
  | 'settings';

export function isPage(value: any): value is Page {
  return ['securities', 'depots', 'dividends', 'settings'].includes(value);
}
