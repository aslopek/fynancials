import {Pipe, PipeTransform} from '@angular/core';
import {SecurityType} from '../../gen/api/security';

@Pipe({
  standalone: true,
  name: 'securityTypeDisplayName'
})
export class SecurityTypeDisplayNamePipe implements PipeTransform {

  transform(securityType?: SecurityType | null): string {
    switch (securityType) {
      case SecurityType.STOCK:
        return 'Stock';
      case SecurityType.ETF:
        return 'ETF';
      case SecurityType.OTHER:
        return 'Other';
      default:
        return '';
    }
  }
}
