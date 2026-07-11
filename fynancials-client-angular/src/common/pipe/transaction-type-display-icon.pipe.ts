import { Pipe, PipeTransform } from '@angular/core';
import { TransactionType } from '../../gen/api/depot-transaction';

@Pipe({
  standalone: true,
  name: 'transactionTypeDisplayIcon'
})
export class TransactionTypeDisplayIconPipe implements PipeTransform {

  transform(transactionType?: TransactionType | null): string {
    switch (transactionType) {
      case TransactionType.BUY:
        return 'move_to_inbox';
      case TransactionType.SELL:
        return 'outbox';
      case TransactionType.DIVIDEND:
        return 'payments';
      case TransactionType.SPECIAL_DIVIDEND:
        return 'payments';
      case TransactionType.TAX:
        return 'percent';
      default:
        return '';
    }
  }
}