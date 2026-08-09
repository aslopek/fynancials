import {beforeEach, describe, expect, it} from '@jest/globals';
import {getState, signalState, SignalState} from '@ngrx/signals';
import {ConfigureStoreState, initialState} from '../../configure.store';
import {togglePasswordVisibility} from './toggle-password-visibility';

describe('togglePasswordVisibility', (): void => {
  let store: SignalState<ConfigureStoreState>;

  beforeEach((): void => {
    store = signalState<ConfigureStoreState>({...initialState});
  });

  it('reveal password', (): void => {
    togglePasswordVisibility(store);

    expect(getState(store)).toEqual({...initialState, passwordVisible: true});
  });

  describe('when password is revealed', (): void => {
    beforeEach((): void => {
      store = signalState<ConfigureStoreState>({...initialState, passwordVisible: true});
    });

    it('hide password', (): void => {
      togglePasswordVisibility(store);

      expect(getState(store)).toEqual({...initialState, passwordVisible: false});
    });
  });
});
