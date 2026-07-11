import {DepotState} from '../depot.state';
import {SetDepotsSliceActionArgs} from '../depot.actions';

export function setDepotSliceReducer(state: Readonly<DepotState>, actionArgs: SetDepotsSliceActionArgs): DepotState {
  let {
    depots,
    selectedDepotIds,
  } = actionArgs;

  let selectedDepotCurrency: string = state.selectedDepotCurrency;

  // if depots exist but none are selected, select the first depot
  if (depots.length > 0 && selectedDepotIds.length === 0) {
    selectedDepotIds = [depots[0].id];
    selectedDepotCurrency = depots[0].currency;
  }

  let selectionIsValid: boolean = true;
  const availableDepotIds: number[] = depots.map(depot => depot.id);
  for (const selectedDepotId of selectedDepotIds) {
    if (!availableDepotIds.includes(selectedDepotId)) {
      selectionIsValid = false;
      break;
    }
  }

  if (selectionIsValid) {
    return {
      ...actionArgs,
      selectedDepotCurrency,
      selectedDepotIds
    };
  }

  if (depots.length === 0) {
    return {
      ...actionArgs,
      selectedDepotCurrency,
      selectedDepotIds: []
    };
  }

  return {
    ...actionArgs,
    selectedDepotCurrency: depots[0].currency,
    selectedDepotIds: [depots[0].id]
  };
}
