import {
  SecuritiesById,
  SecurityState
} from '../security.state';
import {SecurityRead} from '../../../gen/api/security';

export function overwriteSecurities(state: SecurityState, securities: SecurityRead[]): SecurityState {
  if (securities.length === 0) {
    return state;
  }

  const securitiesById: SecuritiesById = {
    ...state.securities
  };

  for (const security of securities) {
    securitiesById[security.id] = security;
  }

  return {
    ...state,
    securities: securitiesById
  };
}