import {beforeEach, describe, expect, it} from '@jest/globals';
import {getState, signalState, SignalState} from '@ngrx/signals';
import {ConfigureStoreState, initialState} from '../../configure.store';
import {setPasswordConfirmation} from './set-password-confirmation';

describe('setPasswordConfirmation', (): void => {
  let store: SignalState<ConfigureStoreState>;

  beforeEach((): void => {
    store = signalState<ConfigureStoreState>({...initialState});
  });

  it('adopts the typed confirmation', (): void => {
    setPasswordConfirmation(store, 'hunter2');

    expect(getState(store)).toEqual({...initialState, passwordConfirmation: 'hunter2'});
  });

  describe('with a password already typed', (): void => {
    beforeEach((): void => {
      store = signalState<ConfigureStoreState>({...initialState, password: 'hunter2'});
    });

    it('leaves the password alone', (): void => {
      setPasswordConfirmation(store, 'hunter3');

      expect(getState(store)).toEqual({
        ...initialState,
        password: 'hunter2',
        passwordConfirmation: 'hunter3'
      });
    });
  });
});
