import {Pipe, PipeTransform, Signal} from '@angular/core';
import {SecurityRead} from '../../gen/api/security';
import {Store} from '@ngrx/store';
import {securitiesById} from '../../store/security/security.selector';
import {SecuritiesById} from '../../store/security/security.state';
import {AppState} from '../../store/app.state';
import {isDevModeActive} from '../../store/app-config/app-config.selector';

@Pipe({
  standalone: true,
  name: 'securityName',
  pure: false
})
export class SecurityNamePipe implements PipeTransform {

  private readonly securities: Signal<SecuritiesById>;
  private readonly devMode: Signal<boolean>;

  constructor(store: Store<AppState>) {
    this.securities = store.selectSignal(securitiesById);
    this.devMode = store.selectSignal(isDevModeActive);
  }

  transform(securityId: number): string {
    const security: SecurityRead | null = this.securities()[securityId];

    if (security == null) {
      return `Security ${securityId}`;
    }

    const securityName: string = security.name;

    if (this.devMode()) {
      return `${securityName} (${security.id})`;
    }
    return securityName;
  }
}