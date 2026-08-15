import {SecurityRead} from '../../../gen/api/security';
import {SecurityState} from '../security.state';

export function overwriteSecurity(state: SecurityState, security: SecurityRead): SecurityState {
  return {
    ...state,
    securities: {
      ...state.securities,
      [security.id]: security
    }
  };
}
