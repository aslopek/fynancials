import {beforeEach, describe, expect, it} from '@jest/globals';
import {getState, signalState, SignalState} from '@ngrx/signals';
import {initialState, UnlockState} from '../unlock.store';
import {setPassword} from './set-password';

describe('setPassword', (): void => {
  let store: SignalState<UnlockState>;

  beforeEach((): void => {
    store = signalState<UnlockState>({...initialState});
  });

  it('sets the password', (): void => {
    setPassword(store, 'hunter2');

    expect(getState(store)).toEqual({...initialState, password: 'hunter2'});
  });

  describe('when passwordMatches was true', (): void => {
    beforeEach((): void => {
      store = signalState<UnlockState>({...initialState, passwordMatches: true});
    });

    it('clears passwordMatches synchronously', (): void => {
      setPassword(store, 'hunter3');

      expect(getState(store)).toEqual({...initialState, password: 'hunter3', passwordMatches: false});
    });
  });
});
