import {getCurrencySymbol} from '@angular/common';
import {inject, Pipe, PipeTransform, Signal} from '@angular/core';
import {Store} from '@ngrx/store';
import {getCurrencyLocale} from '../../store/app-config/app-config.selector';
import {AppState} from '../../store/app.state';

@Pipe({
  name: 'fyCurrencySymbol',
  standalone: true
})
export class FyCurrencySymbolPipe implements PipeTransform {

  private readonly currencyLocale: Signal<string> = inject(Store<AppState>).selectSignal(getCurrencyLocale);

  transform(value: string): string {
    return getCurrencySymbol(value, 'narrow', this.currencyLocale());
  }
}
