import {SecurityRead, SecurityType} from '../gen/api/security';

export function securityReadFactory(overrides?: Partial<SecurityRead>): SecurityRead {
  return {
    isin: 'US0378331005',
    symbols: ['AAPL'],
    name: 'Apple Inc.',
    securityType: SecurityType.STOCK,
    version: 0,
    id: 1,
    _links: {},
    ...overrides
  };
}
