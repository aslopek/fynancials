import {DepotPosition} from '../gen/api/depot-position';

export function depotPositionFactory(overrides?: Partial<DepotPosition>): DepotPosition {
  return {
    securityIds: [1],
    displayName: 'Apple Inc.',
    count: 10,
    buyInAbsolute: 1000,
    buyInRelative: 10,
    currentSizeAbsolute: 1200,
    currentSizeRelative: 12,
    performanceAbsolute: 200,
    performanceRelative: 20,
    ...overrides
  };
}
