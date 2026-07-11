import {CommonModule} from "@angular/common";
import {AfterViewInit, Component, DestroyRef, inject, signal, ViewChild, WritableSignal,} from "@angular/core";
import {takeUntilDestroyed} from "@angular/core/rxjs-interop";
import {MatIconModule} from "@angular/material/icon";
import {MatDrawer, MatSidenavModule} from "@angular/material/sidenav";
import {Router, RouterLink, RouterOutlet} from "@angular/router";
import {Store} from "@ngrx/store";
import {firstValueFrom, Observable} from "rxjs";
import {ConfigApi} from "../gen/api/configuration";
import {AppConfigActions} from "../store/app-config/app-config.actions";
import {getOpenPage, isSideMenuOpen,} from "../store/app-config/app-config.selector";
import {DividendAnnouncementActions} from "../store/dividend-announcement/dividend-announcement.actions";
import {HeaderComponent} from "./header/header.component";
import {Page} from "./page.type";
import {SplashScreenComponent} from "./splash-screen/splash-screen.component";
import {AppState} from "../store/app.state";
import {AppActions} from "../store/app.actions";
import {securityPageStore} from "../security/security-page-store/security-page.store";

@Component({
  selector: "app-root",
  imports: [
    CommonModule,
    RouterOutlet,
    MatSidenavModule,
    HeaderComponent,
    SplashScreenComponent,
    MatIconModule,
    RouterLink,
  ],
  providers: [securityPageStore],
  templateUrl: "app.component.html",
  styleUrls: ["app.component.scss"],
})
export class AppComponent implements AfterViewInit {

  title = "Fynancials";
  protected readonly backendAvailable: WritableSignal<boolean>;
  @ViewChild("drawer") protected drawer!: MatDrawer;

  private readonly store: Store<AppState> = inject(Store);
  private readonly configApi: ConfigApi = inject(ConfigApi);
  protected readonly sideMenuOpen$: Observable<boolean> =
    this.store.select(isSideMenuOpen);
  protected readonly openPage$: Observable<Page> =
    this.store.select(getOpenPage);

  constructor(
    private readonly router: Router,
    destroyRef: DestroyRef,
  ) {
    this.backendAvailable = signal<boolean>(false);
    this.openPage$
      .pipe(takeUntilDestroyed(destroyRef))
      .subscribe((page) => this.router.navigate([`/${page}`]));
  }

  async ngAfterViewInit(): Promise<void> {
    await this.waitForBackend();
    this.backendAvailable.set(true);
    this.store.dispatch(
      DividendAnnouncementActions.loadDividendAnnouncements(),
    );
    this.store.dispatch(AppActions.initialize());
  }

  /**
   * The packaged app spawns the backend as a child process; it needs several seconds to boot. Dispatching the initial
   * load actions before it accepts connections would fail without retry and leave the app empty.
   */
  private async waitForBackend(): Promise<void> {
    while (true) {
      try {
        await firstValueFrom(this.configApi.getPid());
        return;
      } catch {
        await new Promise<void>((resolve) => setTimeout(() => resolve(), 500));
      }
    }
  }

  protected async selectPage(page: Page): Promise<void> {
    this.store.dispatch(AppConfigActions.setOpenPage({openPage: page}));
  }
}
