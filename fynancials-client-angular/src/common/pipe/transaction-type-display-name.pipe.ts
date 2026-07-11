import { Pipe, PipeTransform } from '@angular/core';
import { TransactionType } from '../../gen/api/depot-transaction';

@Pipe({
  standalone: true,
  name: 'transactionTypeDisplayName'
})
export class TransactionTypeDisplayNamePipe implements PipeTransform {

  transform(transactionType?: TransactionType | null): string {
    switch (transactionType) {
      case TransactionType.BUY:
        return 'Buy';
      case TransactionType.SELL:
        return 'Sell';
      case TransactionType.DIVIDEND:
        return 'Dividend';
      case TransactionType.SPECIAL_DIVIDEND:
        return 'Special Dividend';
      case TransactionType.TAX:
        return 'Tax';
      default:
        return '';
    }
  }
}