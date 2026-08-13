import {beforeEach, describe, expect, it, jest} from '@jest/globals';
import {Router} from '@angular/router';
import {getState, signalState, SignalState} from '@ngrx/signals';
import {startupRouteFor} from '../routing/startup-route';
import {initialState, StartupPhase, StartupStoreState} from '../startup.store';
import {enterUnlock} from './enter-unlock';

jest.mock('../routing/startup-route', () => ({
  startupRouteFor: jest.fn()
}));

type StartupRouteFor = (phase: StartupPhase) => string | null;

describe('enterUnlock', (): void => {
  let store: SignalState<StartupStoreState>;
  let navigate: jest.Mock<Router['navigate']>;
  let router: Pick<Router, 'navigate'>;
  let startupRouteForMock: jest.Mock<StartupRouteFor>;

  beforeEach((): void => {
    store = signalState<StartupStoreState>({...initialState});
    navigate = jest.fn<Router['navigate']>();
    router = {navigate};

    startupRouteForMock = startupRouteFor as jest.Mock<StartupRouteFor>;
    startupRouteForMock.mockReset();
    startupRouteForMock.mockReturnValue('/unlock');
  });

  it('enters the unlock phase and navigates to the route of that phase', (): void => {
    enterUnlock(store, router);

    expect(getState(store)).toEqual({...initialState, phase: 'unlock'});
    expect(startupRouteForMock).toHaveBeenCalledTimes(1);
    expect(startupRouteForMock).toHaveBeenCalledWith('unlock');
    expect(navigate).toHaveBeenCalledTimes(1);
    expect(navigate).toHaveBeenCalledWith(['/unlock']);
  });

  describe('when a previous start failed', (): void => {
    beforeEach((): void => {
      store = signalState<StartupStoreState>({...initialState, startFailed: true});
    });

    it('clears the start failure', (): void => {
      enterUnlock(store, router);

      expect(getState(store)).toEqual({...initialState, phase: 'unlock', startFailed: false});
    });
  });
});
