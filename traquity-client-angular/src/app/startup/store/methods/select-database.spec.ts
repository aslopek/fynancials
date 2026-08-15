import {beforeEach, describe, expect, it} from '@jest/globals';
import {getState, signalState, SignalState} from '@ngrx/signals';
import {initialState, StartupStoreState} from '../startup.store';
import {selectDatabase} from './select-database';

describe('selectDatabase', (): void => {
  const databasePath: string = 'D:\\backup\\traquity-test';

  let store: SignalState<StartupStoreState>;

  beforeEach((): void => {
    store = signalState<StartupStoreState>({
      ...initialState,
      authState: 'scrypt',
      databasePath: 'C:\\Users\\x\\traquity',
      mode: 'configure',
      phase: 'configure'
    });
  });

  it('replaces the database path and its auth state', (): void => {
    selectDatabase(store, {databasePath, authState: 'passwordless'});

    expect(getState(store)).toEqual({
      ...initialState,
      authState: 'passwordless',
      databasePath,
      mode: 'configure',
      phase: 'configure'
    });
  });
});
