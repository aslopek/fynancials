import {Routes} from '@angular/router';
import {ConfigureComponent} from './configure/configure.component';
import {DepotPageComponent} from '../depot/depot-page/depot-page.component';
import {DividendsPageComponent} from '../dividends/dividends-page/dividends-page.component';
import {SecurityPageComponent} from '../security/security-page/security-page.component';
import {SettingsPageComponent} from '../settings/settings-page/settings-page.component';
import {ShellComponent} from './shell/shell.component';
import {startupPhaseGuard} from './startup/startup-phase.guard';
import {UnlockComponent} from './unlock/unlock.component';

export const routes: Routes = [
  {
    path: 'unlock',
    component: UnlockComponent
  },
  {
    path: 'configure',
    component: ConfigureComponent
  },
  {
    path: '',
    component: ShellComponent,
    canActivate: [startupPhaseGuard],
    children: [
      {path: 'securities', component: SecurityPageComponent},
      {path: 'depots', component: DepotPageComponent},
      {path: 'dividends', component: DividendsPageComponent},
      {path: 'settings', component: SettingsPageComponent},
      {path: '', pathMatch: 'full', redirectTo: 'securities'}
    ]
  }
];
