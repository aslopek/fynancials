import {DepotState} from '../depot.state';

export type GetSelectedDepotCurrencyState = Pick<DepotState, 'selectedDepotCurrency'>;

export function getSelectedDepotCurrency(state: GetSelectedDepotCurrencyState): string {
  return state.selectedDepotCurrency;
}
