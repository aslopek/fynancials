import {beforeEach, describe, expect, it} from '@jest/globals';
import {getState, signalState, SignalState} from '@ngrx/signals';
import {ConfigureStoreState, initialState} from '../../configure.store';
import {initializeSelection} from './initialize-selection';

describe('initializeSelection', (): void => {
  const databasePath: string = 'C:\\Users\\x\\traquity';

  let store: SignalState<ConfigureStoreState>;

  beforeEach((): void => {
    store = signalState<ConfigureStoreState>({...initialState});
  });

  it('seeds the selection and leaves the origin unchanged', (): void => {
    initializeSelection(store, databasePath);

    expect(getState(store)).toEqual({...initialState, selectedDatabasePath: databasePath});
  });

  it('resets an origin left over from an earlier selection', (): void => {
    store = signalState<ConfigureStoreState>({...initialState, selectionOrigin: 'created'});

    initializeSelection(store, databasePath);

    expect(getState(store)).toEqual({...initialState, selectedDatabasePath: databasePath});
  });

  it('selects nothing on a first run', (): void => {
    initializeSelection(store, null);

    expect(getState(store)).toEqual(initialState);
  });
});
