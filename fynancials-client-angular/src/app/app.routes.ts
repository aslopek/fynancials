import {Routes} from '@angular/router';
import {DepotPageComponent} from '../depot/depot-page/depot-page.component';
import {DividendsPageComponent} from '../dividends/dividends-page/dividends-page.component';
import {SecurityPageComponent} from '../security/security-page/security-page.component';
import {SettingsPageComponent} from '../settings/settings-page/settings-page.component';

export const routes: Routes = [
  {
    path: 'securities',
    component: SecurityPageComponent
  },
  { path: 'depots', component: DepotPageComponent },
  { path: 'dividends', component: DividendsPageComponent },
  { path: 'settings', component: SettingsPageComponent }
];
