import {beforeEach, describe, expect, it} from '@jest/globals';
import {getState, signalState, SignalState} from '@ngrx/signals';
import {initialState, UnlockState} from '../unlock.store';
import {setPasswordMatches} from './set-password-matches';

describe('setPasswordMatches', (): void => {
  let store: SignalState<UnlockState>;

  beforeEach((): void => {
    store = signalState<UnlockState>({...initialState});
  });

  it('sets passwordMatches to true', (): void => {
    setPasswordMatches(store, true);

    expect(getState(store)).toEqual({...initialState, passwordMatches: true});
  });

  describe('when passwordMatches was true', (): void => {
    beforeEach((): void => {
      store = signalState<UnlockState>({...initialState, passwordMatches: true});
    });

    it('sets passwordMatches to false', (): void => {
      setPasswordMatches(store, false);

      expect(getState(store)).toEqual({...initialState, passwordMatches: false});
    });
  });
});
