import {computed, Signal} from '@angular/core';
import {SecurityRead} from '../../../../../gen/api/security';
import {SecuritiesById} from '../../../../../store/security/security.state';

export type SecuritiesLookup = {
  byIsin: Map<string, SecurityRead>
  byName: Map<string, SecurityRead>
  bySymbol: Map<string, SecurityRead>
};

export function securitiesLookup(securitiesById: Signal<SecuritiesById>): Signal<SecuritiesLookup> {
  return computed((): SecuritiesLookup => {
    const byIsin: Map<string, SecurityRead> = new Map<string, SecurityRead>();
    const byName: Map<string, SecurityRead> = new Map<string, SecurityRead>();
    const bySymbol: Map<string, SecurityRead> = new Map<string, SecurityRead>();

    for (const security of Object.values(securitiesById())) {
      byIsin.set(security.isin.toUpperCase(), security);
      byName.set(security.name.trim().toLowerCase(), security);
      for (const symbol of security.symbols) {
        bySymbol.set(symbol.toUpperCase(), security);
      }
    }

    return {byIsin, byName, bySymbol};
  });
}
