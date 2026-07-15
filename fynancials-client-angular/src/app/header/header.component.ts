import {Component, inject, Signal,} from "@angular/core";
import {toSignal} from "@angular/core/rxjs-interop";
import {MatButtonModule} from "@angular/material/button";
import {MatDialog} from "@angular/material/dialog";
import {MatIconModule} from "@angular/material/icon";
import {MatToolbarModule} from "@angular/material/toolbar";
import {MatTooltipModule} from "@angular/material/tooltip";
import {Store} from "@ngrx/store";
import {catchError, Observable, of, switchMap, take} from "rxjs";
import {ConfigApi, DatabaseConfig} from "../../gen/api/configuration";
import {AppConfigActions} from "../../store/app-config/app-config.actions";
import {getOpenPage, isDevModeActive, isSideMenuOpen,} from "../../store/app-config/app-config.selector";
import {DatabaseComponent} from "../database/database.component";
import {InfoComponent} from "../info/info.component";
import {NotificationsComponent} from "./notifications/notifications.component";
import {AppState} from "../../store/app.state";
import {Page} from "../page.type";
import {DepotControlsComponent} from "../../depot/depot-controls/depot-controls.component";
import {SecurityControlsComponent} from "../../security/security-controls/security-controls.component";
import {UpdateIndicatorComponent} from "./update-indicator/update-indicator.component";

@Component({
  selector: "app-header",
  imports: [
    MatToolbarModule,
    MatIconModule,
    MatButtonModule,
    MatTooltipModule,
    NotificationsComponent,
    SecurityControlsComponent,
    DepotControlsComponent,
    UpdateIndicatorComponent,
  ],
  templateUrl: "header.component.html",
  styleUrls: ["header.component.scss"],
})
export class HeaderComponent {
  private readonly appConfigStore: Store<AppState> = inject(Store);
  private readonly sideMenuOpen$: Observable<boolean> = this.appConfigStore.select(isSideMenuOpen);
  protected readonly openPage: Signal<Page> = this.appConfigStore.selectSignal(getOpenPage);
  protected readonly isDevModeActive: Signal<boolean> = this.appConfigStore.selectSignal(isDevModeActive);
  protected readonly databaseConfig: Signal<DatabaseConfig | undefined> = toSignal(this.appConfigStore.select(isDevModeActive).pipe(
      switchMap((devModeActive: boolean): Observable<DatabaseConfig | undefined> =>
        devModeActive
          ? this.configApi.getDatabaseConfig().pipe(catchError((): Observable<undefined> => of(undefined)))
          : of(undefined)
      )
    ),
    {initialValue: undefined}
  );

  constructor(
    private readonly dialog: MatDialog,
    private readonly configApi: ConfigApi,
  ) {
  }

  protected toggleMenu(): void {
    this.sideMenuOpen$.pipe(take(1)).subscribe((isOpen) => {
      this.appConfigStore.dispatch(
        AppConfigActions.setSideMenuOpen({sideMenuOpen: !isOpen}),
      );
    });
  }

  protected openAboutDialog(): void {
    this.dialog.open(InfoComponent, {
      width: "30%",
      height: "50%",
      panelClass: "mat-app-background",
      autoFocus: false,
      disableClose: true,
    });
  }

  protected openDatabaseDialog(): void {
    const databaseConfig: DatabaseConfig | undefined = this.databaseConfig();
    if (databaseConfig == null) {
      return;
    }
    this.dialog.open(DatabaseComponent, {
      width: "95%",
      height: "90%",
      panelClass: "mat-app-background",
      autoFocus: false,
      disableClose: true,
      data: databaseConfig,
    });
  }

  protected openNewTab(url: string): void {
    window.open(url, "_blank");
  }
}
