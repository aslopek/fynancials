import {Actions, ofType} from '@ngrx/effects';
import {map, merge, mergeMap, Observable} from 'rxjs';
import {Action} from '@ngrx/store';
import {SecurityActions} from '../security.actions';
import {DividendAnnouncementActions} from '../../dividend-announcement/dividend-announcement.actions';

export type LoadSecuritiesOnEffectArgs = {
  actions$: Actions
};

type SecurityIdObject = {
  securityId: number
};

function toSecurityIds(items: (number | SecurityIdObject)[]): number[] {
  return [...new Set(items.map((item: number | SecurityIdObject): number => typeof item === 'number' ? item : item.securityId))];
}

/**
 * Listens for other domains' Done/Success actions that reference securities needing a (re)load, rather than having those domains
 * dispatch SecurityActions.loadSecurity themselves. To add a trigger, add a branch to the `merge(...)` below that maps the
 * new action to a `(number | SecurityIdObject)[]` - the id normalization and dispatching below is shared by all triggers.
 */
export function loadSecuritiesOn(effectArgs: LoadSecuritiesOnEffectArgs): Observable<Action> {
  const {actions$} = effectArgs;
  return merge(
    actions$.pipe(
      ofType(DividendAnnouncementActions.loadDividendAnnouncementsSuccess),
      map(({dividendAnnouncements}): (number | SecurityIdObject)[] => dividendAnnouncements)
    )
  ).pipe(
    mergeMap((items: (number | SecurityIdObject)[]): Action[] =>
      toSecurityIds(items).map((securityId: number): Action => SecurityActions.loadSecurity({securityId})))
  );
}
