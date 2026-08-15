import {beforeEach, describe, expect, it} from '@jest/globals';
import {getState, signalState, SignalState} from '@ngrx/signals';
import {initialState, UnlockState} from '../unlock.store';
import {togglePasswordVisibility} from './toggle-password-visibility';

describe('togglePasswordVisibility', (): void => {
  let store: SignalState<UnlockState>;

  beforeEach((): void => {
    store = signalState<UnlockState>({...initialState});
  });

  it('toggles password visibility to true', (): void => {
    togglePasswordVisibility(store);

    expect(getState(store)).toEqual({...initialState, passwordVisible: true});
  });

  describe('when passwordVisible was true', (): void => {
    beforeEach((): void => {
      store = signalState<UnlockState>({...initialState, passwordVisible: true});
    });

    it('toggles password visibility to false', (): void => {
      togglePasswordVisibility(store);

      expect(getState(store)).toEqual({...initialState, passwordVisible: false});
    });
  });
});
