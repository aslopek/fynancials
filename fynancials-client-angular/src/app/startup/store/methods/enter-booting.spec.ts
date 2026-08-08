import {beforeEach, describe, expect, it, jest} from '@jest/globals';
import {Router} from '@angular/router';
import {getState, signalState, SignalState} from '@ngrx/signals';
import {initialState, StartupStoreState} from '../startup.store';
import {enterBooting} from './enter-booting';

describe('enterBooting', (): void => {
  let store: SignalState<StartupStoreState>;
  let navigate: jest.Mock<Router['navigate']>;
  let router: Pick<Router, 'navigate'>;

  beforeEach((): void => {
    store = signalState<StartupStoreState>({...initialState});
    navigate = jest.fn<Router['navigate']>();
    router = {navigate};
  });

  it('stays in the booting phase without navigating', (): void => {
    enterBooting(store, router);

    expect(getState(store)).toEqual(initialState);
    expect(navigate).not.toHaveBeenCalled();
  });

  describe('when the phase is unlock', (): void => {
    beforeEach((): void => {
      store = signalState<StartupStoreState>({...initialState, phase: 'unlock'});
    });

    it('enters the booting phase', (): void => {
      enterBooting(store, router);

      expect(getState(store)).toEqual({...initialState, phase: 'booting'});
    });

    it('navigates to the shell', (): void => {
      enterBooting(store, router);

      expect(navigate).toHaveBeenCalledTimes(1);
      expect(navigate).toHaveBeenCalledWith(['/']);
    });
  });
});
