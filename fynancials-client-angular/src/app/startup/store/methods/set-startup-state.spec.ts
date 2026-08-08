import {beforeEach, describe, expect, it} from '@jest/globals';
import {getState, signalState, SignalState} from '@ngrx/signals';
import {StartupState} from '../../startup-bridge.type';
import {initialState, StartupStoreState} from '../startup.store';
import {setStartupState} from './set-startup-state';

describe('setStartupState', (): void => {
  const databasePath: string = 'C:\\Users\\x\\fynancials';

  let store: SignalState<StartupStoreState>;

  beforeEach((): void => {
    store = signalState<StartupStoreState>({...initialState});
  });

  it('enters the booting phase for boot mode', (): void => {
    const state: StartupState = {databasePath, mode: 'boot'};

    setStartupState(store, state);

    expect(getState(store)).toEqual({databasePath, mode: 'boot', phase: 'booting'});
  });

  it('enters the unlock phase for unlock mode', (): void => {
    const state: StartupState = {databasePath, mode: 'unlock'};

    setStartupState(store, state);

    expect(getState(store)).toEqual({databasePath, mode: 'unlock', phase: 'unlock'});
  });

  it('enters the configure phase for configure mode', (): void => {
    const state: StartupState = {databasePath, mode: 'configure'};

    setStartupState(store, state);

    expect(getState(store)).toEqual({databasePath, mode: 'configure', phase: 'configure'});
  });

  it('records a null database path when the config names no database', (): void => {
    const state: StartupState = {databasePath: null, mode: 'configure'};

    setStartupState(store, state);

    expect(getState(store)).toEqual({databasePath: null, mode: 'configure', phase: 'configure'});
  });
});
