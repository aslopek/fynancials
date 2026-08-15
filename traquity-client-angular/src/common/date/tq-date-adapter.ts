import {inject, Injectable} from '@angular/core';
import {NativeDateAdapter} from '@angular/material/core';
import {Store} from '@ngrx/store';
import {getDateFormat, getDateLocale} from '../../store/app-config/app-config.selector';
import {AppState} from '../../store/app.state';
import {TqDatePipe} from '../pipe/tq-date.pipe';

@Injectable()
export class TqDateAdapter extends NativeDateAdapter {
  private readonly tqDatePipe: TqDatePipe = inject(TqDatePipe);

  constructor() {
    super();
    const store: Store<AppState> = inject(Store);
    store.select(getDateFormat).subscribe((): void => this._localeChanges.next());
    store.select(getDateLocale).subscribe((): void => this._localeChanges.next());
  }

  override format(date: Date): string {
    return this.tqDatePipe.transform(date);
  }
}
