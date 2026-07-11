import { Pipe, PipeTransform } from '@angular/core';
import { TransactionType } from '../../gen/api/depot-transaction';

@Pipe({
  name: 'fyNetValue',
  standalone: true
})
export class FyNetValuePipe implements PipeTransform {

  transform(value: {
    transactionType: TransactionType,
    grossValue: number | string,
    tax?: number | string,
    fee?: number | string
  } | null | undefined): number {
    if (value === null || value === undefined) {
      return 0;
    }
    const transactionType: TransactionType = value.transactionType;
    const grossValue: number = this.parseNumber(value.grossValue);
    const tax: number = this.parseNumber(value.tax ?? 0);
    const fee: number = this.parseNumber(value.fee ?? 0);

    if (transactionType === TransactionType.BUY || transactionType === TransactionType.TAX) {
      return grossValue + tax + fee;
    } else {
      return grossValue - tax - fee;
    }
  }

  private parseNumber(value: number | string): number {
    if (typeof value === 'number') {
      return value;
    } else if (value === '') {
      return 0;
    }

    return parseFloat(value.replace(',', '.'));
  }
}