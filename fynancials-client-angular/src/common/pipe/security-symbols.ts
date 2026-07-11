import {
  inject,
  Pipe,
  PipeTransform,
  Signal
} from '@angular/core';
import {SecuritiesById} from '../../store/security/security.state';
import {Store} from '@ngrx/store';
import {securitiesById} from '../../store/security/security.selector';
import {SecurityRead} from '../../gen/api/security';

@Pipe({
  name: 'securitySymbols'
})
export class SecuritySymbols implements PipeTransform {

  private readonly securities: Signal<SecuritiesById> = inject(Store).selectSignal(securitiesById);

  transform(securityId: number): string {
    const security: SecurityRead | null = this.securities()[securityId];
    if (security == null) {
      return '';
    } else if (security.symbols.length === 0) {
      return '-';
    }

    return security.symbols.join(' ');
  }
}
