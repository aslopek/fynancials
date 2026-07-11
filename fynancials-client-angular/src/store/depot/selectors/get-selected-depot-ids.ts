import {DepotState} from '../depot.state';

export type GetSelectedDepotIdsState = Pick<DepotState, 'selectedDepotIds'>;

export function getSelectedDepotIds(state: GetSelectedDepotIdsState): number[] {
  return state.selectedDepotIds;
}
