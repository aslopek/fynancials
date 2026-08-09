import {beforeEach, describe, expect, it} from '@jest/globals';
import {getState, signalState, SignalState} from '@ngrx/signals';
import {ConfigureStoreState, initialState} from '../../configure.store';
import {selectExistingDatabase} from './select-existing-database';

describe('selectExistingDatabase', (): void => {
  const databasePath: string = 'D:\\backup\\fynancials-test';

  let store: SignalState<ConfigureStoreState>;

  beforeEach((): void => {
    store = signalState<ConfigureStoreState>({...initialState});
  });

  it('adopts the file as a picked one', (): void => {
    selectExistingDatabase(store, databasePath);

    expect(getState(store)).toEqual({
      ...initialState,
      selectedDatabasePath: databasePath,
      selectionOrigin: 'picked'
    });
  });

  describe('after a database was created here', (): void => {
    beforeEach((): void => {
      store = signalState<ConfigureStoreState>({
        ...initialState,
        password: 'hunter2',
        passwordConfirmation: 'hunter2',
        selectedDatabasePath: 'C:\\Users\\x\\fynancials',
        selectionOrigin: 'created'
      });
    });

    it('clears the password typed for it', (): void => {
      selectExistingDatabase(store, databasePath);

      expect(getState(store)).toEqual({
        ...initialState,
        selectedDatabasePath: databasePath,
        selectionOrigin: 'picked'
      });
    });
  });
});
