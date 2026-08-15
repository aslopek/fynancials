import {DecimalPipe} from '@angular/common';
import {inject, Pipe, PipeTransform, Signal} from '@angular/core';
import {Store} from '@ngrx/store';
import {getDecimalLocale} from '../../store/app-config/app-config.selector';
import {AppState} from '../../store/app.state';

@Pipe({
  name: 'tqDecimal',
  standalone: true
})
export class TqDecimalPipe implements PipeTransform {

  private readonly decimalLocale: Signal<string> = inject(Store<AppState>).selectSignal(getDecimalLocale);

  transform(value: string | number, digitsInfo: string = '1.2-2'): string {
    return new DecimalPipe(this.decimalLocale()).transform(value, digitsInfo) ?? '';
  }
}
