import {SecurityRead} from '../../../gen/api/security';
import {SecurityState} from '../security.state';

export function getSecurityByIdSelector(state: SecurityState, id: number): SecurityRead | null {
  return state.securities[id] ?? null;
}
