import {DatePipe} from '@angular/common';
import {inject, Pipe, PipeTransform, Signal} from '@angular/core';
import {Store} from '@ngrx/store';
import {getDateFormat, getDateLocale} from '../../store/app-config/app-config.selector';
import {AppState} from '../../store/app.state';

@Pipe({
  name: 'fyDate',
  standalone: true
})
export class FyDatePipe implements PipeTransform {

  private readonly dateFormat: Signal<string> = inject(Store<AppState>).selectSignal(getDateFormat);
  private readonly dateLocale: Signal<string> = inject(Store<AppState>).selectSignal(getDateLocale);

  transform(value: string | number | Date): string {
    return new DatePipe(this.dateLocale()).transform(value, this.dateFormat()) ?? '';
  }
}
