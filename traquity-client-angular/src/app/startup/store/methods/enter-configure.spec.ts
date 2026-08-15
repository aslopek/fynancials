import {beforeEach, describe, expect, it, jest} from '@jest/globals';
import {Router} from '@angular/router';
import {getState, signalState, SignalState} from '@ngrx/signals';
import {startupRouteFor} from '../routing/startup-route';
import {initialState, StartupPhase, StartupStoreState} from '../startup.store';
import {enterConfigure} from './enter-configure';

jest.mock('../routing/startup-route', () => ({
  startupRouteFor: jest.fn()
}));

type StartupRouteFor = (phase: StartupPhase) => string | null;

describe('enterConfigure', (): void => {
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
    startupRouteForMock.mockReturnValue('/configure');
  });

  it('enters the configure phase and navigates to the route of that phase', (): void => {
    enterConfigure(store, router);

    expect(getState(store)).toEqual({...initialState, phase: 'configure'});
    expect(startupRouteForMock).toHaveBeenCalledTimes(1);
    expect(startupRouteForMock).toHaveBeenCalledWith('configure');
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
