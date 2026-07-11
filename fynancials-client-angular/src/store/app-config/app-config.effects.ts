import {inject, Injectable} from '@angular/core';
import {Actions, createEffect, ofType} from '@ngrx/effects';
import {catchError, concatMap, EMPTY, map, of} from 'rxjs';
import {isPage, Page} from '../../app/page.type';
import {ConfigApi} from '../../gen/api/configuration';
import {clientId} from '../client-id';
import {
  appConfigPrefix,
  AppCurrencyLocale,
  AppDateFormat,
  AppDateLocale,
  AppDecimalLocale,
  AppHideAbsoluteValues,
  AppOpenPage,
  AppPercentLocale,
  AppSideMenuOpen
} from './app-config-keys';
import {AppConfigActions} from './app-config.actions';
import {AppActions} from '../app.actions';

@Injectable()
export class AppConfigEffects {

  private readonly actions$: Actions = inject(Actions);
  private readonly configApi: ConfigApi = inject(ConfigApi);

  readonly loadDevModeActive = createEffect(() =>
    this.actions$.pipe(
      ofType(AppActions.initialize),
      concatMap(() =>
        this.configApi.isDevModeActive().pipe(
          map(devModeActive => AppConfigActions.setDevModeActive({
            devModeActive: devModeActive === 'true',
            persist: false
          })),
          catchError(() => EMPTY)
        )
      )
    )
  );

  readonly loadAppState = createEffect(() =>
    this.actions$.pipe(
      ofType(AppActions.initialize),
      concatMap(() =>
        this.configApi.getClientConfig(clientId, appConfigPrefix).pipe(
          map(config => {
            let hideAbsoluteValues: boolean = AppHideAbsoluteValues.default;
            if (config[AppHideAbsoluteValues.key] !== undefined) {
              hideAbsoluteValues = config[AppHideAbsoluteValues.key] === 'true';
            }

            let openPage: Page = AppOpenPage.default;
            let value = config[AppOpenPage.key];
            if (isPage(value)) {
              openPage = value;
            }

            let sideMenuOpen: boolean = AppSideMenuOpen.default;
            if (config[AppSideMenuOpen.key] !== undefined) {
              sideMenuOpen = config[AppSideMenuOpen.key] === 'true';
            }

            let currencyLocale: string = AppCurrencyLocale.default;
            if (config[AppCurrencyLocale.key] !== undefined) {
              currencyLocale = config[AppCurrencyLocale.key];
            }

            let dateFormat: string = AppDateFormat.default;
            if (config[AppDateFormat.key] !== undefined) {
              dateFormat = config[AppDateFormat.key];
            }

            let dateLocale: string = AppDateLocale.default;
            if (config[AppDateLocale.key] !== undefined) {
              dateLocale = config[AppDateLocale.key];
            }

            let decimalLocale: string = AppDecimalLocale.default;
            if (config[AppDecimalLocale.key] !== undefined) {
              decimalLocale = config[AppDecimalLocale.key];
            }

            let percentLocale: string = AppPercentLocale.default;
            if (config[AppPercentLocale.key] !== undefined) {
              percentLocale = config[AppPercentLocale.key];
            }

            return AppConfigActions.setAppConfig({
              hideAbsoluteValues,
              openPage,
              sideMenuOpen,
              dateFormat,
              locales: {
                currency: currencyLocale,
                date: dateLocale,
                decimal: decimalLocale,
                percent: percentLocale
              }
            });

          }),
          catchError(() => EMPTY)
        )
      )
    )
  );

  readonly setCurrencyLocale = createEffect(() =>
    this.actions$.pipe(
      ofType(AppConfigActions.setCurrencyLocale),
      concatMap(({ currencyLocale }) => {
        return this.configApi.setClientConfigValue(clientId, AppCurrencyLocale.key, currencyLocale).pipe(
          map(() => AppConfigActions.setCurrencyLocaleDone({currencyLocale})),
          catchError(() => of(AppConfigActions.setCurrencyLocaleDone({currencyLocale})))
        );
      })
    )
  );

  readonly setDateFormat = createEffect(() =>
    this.actions$.pipe(
      ofType(AppConfigActions.setDateFormat),
      concatMap(({ dateFormat }) => {
        return this.configApi.setClientConfigValue(clientId, AppDateFormat.key, dateFormat).pipe(
          map(() => AppConfigActions.setDateFormatDone({dateFormat})),
          catchError(() => of(AppConfigActions.setDateFormatDone({dateFormat})))
        );
      })
    )
  );

  readonly setDateLocale = createEffect(() =>
    this.actions$.pipe(
      ofType(AppConfigActions.setDateLocale),
      concatMap(({ dateLocale }) => {
        return this.configApi.setClientConfigValue(clientId, AppDateLocale.key, dateLocale).pipe(
          map(() => AppConfigActions.setDateLocaleDone({dateLocale})),
          catchError(() => of(AppConfigActions.setDateLocaleDone({dateLocale})))
        );
      })
    )
  );

  readonly setDecimalLocale = createEffect(() =>
    this.actions$.pipe(
      ofType(AppConfigActions.setDecimalLocale),
      concatMap(({ decimalLocale }) => {
        return this.configApi.setClientConfigValue(clientId, AppDecimalLocale.key, decimalLocale).pipe(
          map(() => AppConfigActions.setDecimalLocaleDone({decimalLocale})),
          catchError(() => of(AppConfigActions.setDecimalLocaleDone({decimalLocale})))
        );
      })
    )
  );

  readonly setHideAbsoluteValues = createEffect(() =>
    this.actions$.pipe(
      ofType(AppConfigActions.setHideAbsoluteValues),
      concatMap(({ hideAbsoluteValues }) => {
        return this.configApi.setClientConfigValue(clientId, AppHideAbsoluteValues.key, `${hideAbsoluteValues}`).pipe(
          map(() => AppConfigActions.setHideAbsoluteValuesDone({hideAbsoluteValues})),
          catchError(() => of(AppConfigActions.setHideAbsoluteValuesDone({hideAbsoluteValues})))
        );
      })
    )
  );

  readonly setOpenPage = createEffect(() =>
    this.actions$.pipe(
      ofType(AppConfigActions.setOpenPage),
      concatMap(({ openPage }) => {
        return this.configApi.setClientConfigValue(clientId, AppOpenPage.key, openPage).pipe(
          map(() => AppConfigActions.setOpenPageDone({openPage})),
          catchError(() => of(AppConfigActions.setOpenPageDone({openPage})))
        );
      })
    )
  );

  readonly setPercentLocale = createEffect(() =>
    this.actions$.pipe(
      ofType(AppConfigActions.setPercentLocale),
      concatMap(({ percentLocale }) => {
        return this.configApi.setClientConfigValue(clientId, AppPercentLocale.key, percentLocale).pipe(
          map(() => AppConfigActions.setPercentLocaleDone({percentLocale})),
          catchError(() => of(AppConfigActions.setPercentLocaleDone({percentLocale})))
        );
      })
    )
  );

  readonly setDevModeActive = createEffect(() =>
    this.actions$.pipe(
      ofType(AppConfigActions.setDevModeActive),
      concatMap(({ devModeActive, persist }) => {
        if (persist) {
          return this.configApi.setDevModeActive(`${devModeActive}`).pipe(
            map(() => AppConfigActions.setDevModeActiveDone({devModeActive})),
            catchError(() => of(AppConfigActions.setDevModeActiveDone({devModeActive})))
          );
        } else {
          return of(AppConfigActions.setDevModeActiveDone({ devModeActive }));
        }
      })
    )
  );

  readonly setSideMenuOpen = createEffect(() =>
    this.actions$.pipe(
      ofType(AppConfigActions.setSideMenuOpen),
      concatMap(({ sideMenuOpen }) =>
        this.configApi.setClientConfigValue(clientId, AppSideMenuOpen.key, `${sideMenuOpen}`).pipe(
          map(() => AppConfigActions.setSideMenuOpenDone({ sideMenuOpen })),
          catchError(() => of(AppConfigActions.setSideMenuOpenDone({sideMenuOpen})))
        )
      )
    )
  );
}
