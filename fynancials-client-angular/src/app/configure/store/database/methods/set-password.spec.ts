import {beforeEach, describe, expect, it} from '@jest/globals';
import {getState, signalState, SignalState} from '@ngrx/signals';
import {ConfigureStoreState, initialState} from '../../configure.store';
import {setPassword} from './set-password';

describe('setPassword', (): void => {
  let store: SignalState<ConfigureStoreState>;

  beforeEach((): void => {
    store = signalState<ConfigureStoreState>({...initialState});
  });

  it('adopts the typed password', (): void => {
    setPassword(store, 'hunter2');

    expect(getState(store)).toEqual({...initialState, password: 'hunter2'});
  });

  describe('with a confirmation already typed', (): void => {
    beforeEach((): void => {
      store = signalState<ConfigureStoreState>({...initialState, passwordConfirmation: 'hunter2'});
    });

    it('leaves the confirmation alone', (): void => {
      setPassword(store, 'hunter3');

      expect(getState(store)).toEqual({
        ...initialState,
        password: 'hunter3',
        passwordConfirmation: 'hunter2'
      });
    });
  });
});
