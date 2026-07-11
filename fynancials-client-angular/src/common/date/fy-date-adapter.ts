import {inject, Injectable} from '@angular/core';
import {NativeDateAdapter} from '@angular/material/core';
import {Store} from '@ngrx/store';
import {getDateFormat, getDateLocale} from '../../store/app-config/app-config.selector';
import {AppState} from '../../store/app.state';
import {FyDatePipe} from '../pipe/fy-date.pipe';

@Injectable()
export class FyDateAdapter extends NativeDateAdapter {
  private readonly fyDatePipe: FyDatePipe = inject(FyDatePipe);

  constructor() {
    super();
    const store: Store<AppState> = inject(Store);
    store.select(getDateFormat).subscribe((): void => this._localeChanges.next());
    store.select(getDateLocale).subscribe((): void => this._localeChanges.next());
  }

  override format(date: Date): string {
    return this.fyDatePipe.transform(date);
  }
}
