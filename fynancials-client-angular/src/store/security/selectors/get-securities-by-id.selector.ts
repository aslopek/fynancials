import {
  SecuritiesById,
  SecurityState
} from '../security.state';

export function getSecuritiesByIdSelector(state: Pick<SecurityState, 'securities'>): SecuritiesById {
  return state.securities;
}
