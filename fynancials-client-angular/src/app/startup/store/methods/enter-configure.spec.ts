import {beforeEach, describe, expect, it, jest} from '@jest/globals';
import {Router} from '@angular/router';
import {getState, signalState, SignalState} from '@ngrx/signals';
import {initialState, StartupStoreState} from '../startup.store';
import {enterConfigure} from './enter-configure';

describe('enterConfigure', (): void => {
  let store: SignalState<StartupStoreState>;
  let navigate: jest.Mock<Router['navigate']>;
  let router: Pick<Router, 'navigate'>;

  beforeEach((): void => {
    store = signalState<StartupStoreState>({...initialState});
    navigate = jest.fn<Router['navigate']>();
    router = {navigate};
  });

  it('enters the configure phase and navigates to /configure', (): void => {
    enterConfigure(store, router);

    expect(getState(store)).toEqual({...initialState, phase: 'configure'});
    expect(navigate).toHaveBeenCalledTimes(1);
    expect(navigate).toHaveBeenCalledWith(['/configure']);
  });

  describe('when a previous start failed', (): void => {
    beforeEach((): void => {
      store = signalState<StartupStoreState>({...initialState, startFailed: true});
    });

    it('clears the start failure', (): void => {
      enterConfigure(store, router);

      expect(getState(store)).toEqual({...initialState, phase: 'configure', startFailed: false});
    });
  });
});
