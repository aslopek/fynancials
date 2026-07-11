import {CurrencyPipe} from '@angular/common';
import {inject, Pipe, PipeTransform, Signal} from '@angular/core';
import {Store} from '@ngrx/store';
import {getCurrencyLocale} from '../../store/app-config/app-config.selector';
import {AppState} from '../../store/app.state';

@Pipe({
  name: 'fyCurrency',
  standalone: true
})
export class FyCurrencyPipe implements PipeTransform {

  private readonly currencyLocale: Signal<string> = inject(Store<AppState>).selectSignal(getCurrencyLocale);

  transform(value: string | number, currencyCode: string, digitsInfo = '1.2-2'): string {
    return new CurrencyPipe(this.currencyLocale()).transform(value, currencyCode, 'symbol', digitsInfo) ?? '';
  }
}
