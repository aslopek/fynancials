import {beforeEach, describe, expect, it} from '@jest/globals';
import {getState, signalState, SignalState} from '@ngrx/signals';
import {ConfigureStoreState, initialState} from '../../configure.store';
import {selectKnownDatabase} from './select-known-database';

describe('selectKnownDatabase', (): void => {
  const databasePath: string = 'D:\\backup\\traquity-test';

  let store: SignalState<ConfigureStoreState>;

  beforeEach((): void => {
    store = signalState<ConfigureStoreState>({...initialState});
  });

  it('adopts the database as one the app already knows', (): void => {
    selectKnownDatabase(store, databasePath);

    expect(getState(store)).toEqual({
      ...initialState,
      selectedDatabasePath: databasePath,
      selectionOrigin: 'known'
    });
  });

  describe('after a database was created here', (): void => {
    beforeEach((): void => {
      store = signalState<ConfigureStoreState>({
        ...initialState,
        password: 'hunter2',
        passwordConfirmation: 'hunter2',
        selectedDatabasePath: 'C:\\Users\\x\\traquity',
        selectionOrigin: 'created'
      });
    });

    it('clears the password typed for it', (): void => {
      selectKnownDatabase(store, databasePath);

      expect(getState(store)).toEqual({
        ...initialState,
        selectedDatabasePath: databasePath,
        selectionOrigin: 'known'
      });
    });
  });
});
