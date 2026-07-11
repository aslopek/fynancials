import {PercentPipe} from '@angular/common';
import {inject, Pipe, PipeTransform, Signal} from '@angular/core';
import {Store} from '@ngrx/store';
import {getPercentLocale} from '../../store/app-config/app-config.selector';
import {AppState} from '../../store/app.state';

@Pipe({
  name: 'fyPercent',
  standalone: true
})
export class FyPercentPipe implements PipeTransform {

  private readonly percentLocale: Signal<string> = inject(Store<AppState>).selectSignal(getPercentLocale);

  transform(value: string | number, digitsInfo: string = '1.2-2', pa: boolean = false): string {
    const formatted: string = new PercentPipe(this.percentLocale()).transform(value, digitsInfo) ?? '';
    if (pa) {
      return `${formatted} p.a.`;
    }
    return formatted;
  }
}
