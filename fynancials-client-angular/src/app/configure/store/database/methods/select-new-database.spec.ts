import {beforeEach, describe, expect, it} from '@jest/globals';
import {getState, signalState, SignalState} from '@ngrx/signals';
import {ConfigureStoreState, initialState} from '../../configure.store';
import {selectNewDatabase} from './select-new-database';

describe('selectNewDatabase', (): void => {
  const databasePath: string = 'D:\\backup\\fynancials-new';

  let store: SignalState<ConfigureStoreState>;

  beforeEach((): void => {
    store = signalState<ConfigureStoreState>({...initialState});
  });

  it('adopts the file as a created one', (): void => {
    selectNewDatabase(store, databasePath);

    expect(getState(store)).toEqual({
      ...initialState,
      selectedDatabasePath: databasePath,
      selectionOrigin: 'created'
    });
  });

  describe('after another database was created here', (): void => {
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
      selectNewDatabase(store, databasePath);

      expect(getState(store)).toEqual({
        ...initialState,
        selectedDatabasePath: databasePath,
        selectionOrigin: 'created'
      });
    });
  });
});
