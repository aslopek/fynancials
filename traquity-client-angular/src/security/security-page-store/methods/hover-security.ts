import {WritableSignalStore} from '../../../common/types/signal-store.type';
import {patchState} from '@ngrx/signals';
import {SecurityPageState} from '../security-page.store';

export function hoverSecurity(signalStore: WritableSignalStore<SecurityPageState>, securityId: number | null): void {
  patchState(signalStore, {
    hoveredSecurityId: securityId
  });
}
