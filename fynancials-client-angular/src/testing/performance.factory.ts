import {Performance} from '../gen/api/depot-performance';

export function performanceFactory(overrides?: Partial<Performance>): Performance {
  return {
    securityIds: [1],
    transactions: [],
    absoluteValueGross: 120,
    absoluteValueNet: 100,
    ...overrides
  };
}
