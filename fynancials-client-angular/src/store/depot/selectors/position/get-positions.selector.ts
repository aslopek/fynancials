import {DepotState, Positions} from "../../depot.state";
import {DepotPosition} from "../../../../gen/api/depot-position";

export type GetPositionsState = {
  position: Pick<DepotState['position'], 'positions' | 'useBuyIn'>
};

export function getPositions(state: GetPositionsState): Positions {
  const result: Positions = state.position.positions ?? {
    buyInAbsolute: 0,
    currentSizeAbsolute: 0,
    absolutePerformance: 0,
    relativePerformance: 0,
    positions: []
  };

  const sortedPositions: DepotPosition[] = [...result.positions];
  if (state.position.useBuyIn) {
    sortedPositions.sort((a, b) => b.buyInAbsolute - a.buyInAbsolute);
  } else {
    sortedPositions.sort((a, b) => b.currentSizeAbsolute - a.currentSizeAbsolute);
  }

  return {
    ...result,
    positions: sortedPositions
  };
}
