import {beforeEach, describe, expect, it, jest} from '@jest/globals';
import {Router} from '@angular/router';
import {getState, signalState, SignalState} from '@ngrx/signals';
import {BackendStartOutcome} from '../../startup-bridge.type';
import {initialState, StartupStoreState} from '../startup.store';
import {applyStartOutcome} from './apply-start-outcome';

describe('applyStartOutcome', (): void => {
  let store: SignalState<StartupStoreState>;
  let navigate: jest.Mock<Router['navigate']>;
  let router: Pick<Router, 'navigate'>;
  let outcome: BackendStartOutcome;

  beforeEach((): void => {
    store = signalState<StartupStoreState>({...initialState});
    navigate = jest.fn<Router['navigate']>();
    router = {navigate};
    outcome = {reachable: true, startedFrom: 'pending'};
  });

  it('does nothing when the outcome is reachable', (): void => {
    applyStartOutcome(store, router, outcome);

    expect(getState(store)).toEqual(initialState);
    expect(navigate).not.toHaveBeenCalled();
  });

  describe('when the outcome is unreachable and started from pending', (): void => {
    beforeEach((): void => {
      outcome = {reachable: false, startedFrom: 'pending'};
    });

    it('enters the unlock phase', (): void => {
      applyStartOutcome(store, router, outcome);

      expect(getState(store)).toEqual({...initialState, phase: 'unlock'});
    });

    it('navigates to /unlock', (): void => {
      applyStartOutcome(store, router, outcome);

      expect(navigate).toHaveBeenCalledTimes(1);
      expect(navigate).toHaveBeenCalledWith(['/unlock']);
    });
  });

  describe('when the outcome is unreachable and started from a verified scrypt record', (): void => {
    beforeEach((): void => {
      outcome = {reachable: false, startedFrom: 'scrypt'};
    });

    it('enters the configure phase', (): void => {
      applyStartOutcome(store, router, outcome);

      expect(getState(store)).toEqual({...initialState, phase: 'configure'});
    });

    it('navigates to /configure', (): void => {
      applyStartOutcome(store, router, outcome);

      expect(navigate).toHaveBeenCalledTimes(1);
      expect(navigate).toHaveBeenCalledWith(['/configure']);
    });
  });

  describe('when the outcome is unreachable and started from a passwordless database', (): void => {
    beforeEach((): void => {
      outcome = {reachable: false, startedFrom: 'passwordless'};
    });

    it('enters the configure phase', (): void => {
      applyStartOutcome(store, router, outcome);

      expect(getState(store)).toEqual({...initialState, phase: 'configure'});
    });

    it('navigates to /configure', (): void => {
      applyStartOutcome(store, router, outcome);

      expect(navigate).toHaveBeenCalledTimes(1);
      expect(navigate).toHaveBeenCalledWith(['/configure']);
    });
  });
});
