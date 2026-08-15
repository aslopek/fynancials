import {SecurityRead} from '../../../gen/api/security';
import {SecurityState} from '../security.state';

export function getSecurityIdsByNameSelector(state: Pick<SecurityState, 'securities'>): { [securityName: string]: number } {
  const securityIdsByName: { [securityName: string]: number } = {};
  for (const security of Object.values(state.securities)) {
    const securityRead: SecurityRead = security;
    securityIdsByName[securityRead.name] = securityRead.id;
  }
  return securityIdsByName;
}
