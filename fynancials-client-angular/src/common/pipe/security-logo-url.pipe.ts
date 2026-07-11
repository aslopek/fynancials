import {
  Pipe,
  PipeTransform
} from '@angular/core';
import {SecurityLogoApi} from '../../gen/api/security';

@Pipe({
  name: 'securityLogoUrl',
  standalone: true
})
export class SecurityLogoUrlPipe implements PipeTransform {

  private readonly logoBasePath: string;

  constructor(securityLogoApi: SecurityLogoApi) {
    this.logoBasePath = securityLogoApi.configuration.basePath ?? '';
  }

  transform(securityId: number): string {
    return `${this.logoBasePath}/securities/${securityId}/logo`;
  }
}
