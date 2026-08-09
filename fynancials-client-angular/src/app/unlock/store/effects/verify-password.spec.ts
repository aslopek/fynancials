import {beforeEach, describe, expect, it, jest} from '@jest/globals';
import {signal, WritableSignal} from '@angular/core';
import {getState, signalState, SignalState} from '@ngrx/signals';
import {Observable} from 'rxjs';
import {RunHelpers, TestScheduler} from 'rxjs/testing';
import {StartupBridgeService} from '../../../startup/startup-bridge.service';
import {AuthState} from '../../../startup/startup-bridge.type';
import {initialState, UnlockState} from '../unlock.store';
import {PASSWORD_VERIFY_DEBOUNCE_MS, verifyPasswordPipe} from './verify-password';
import {HotObservable} from "rxjs/internal/testing/HotObservable";

type VerifyPassword = (password: string) => Observable<boolean>;

describe('verifyPasswordPipe', (): void => {
  // 1 marble character = 1 virtual ms inside scheduler.run(), so this ties the debounce gap to the real constant
  // rather than a hand-picked frame count
  const debounceGap: string = '-'.repeat(PASSWORD_VERIFY_DEBOUNCE_MS);

  let scheduler: TestScheduler;
  let store: SignalState<UnlockState>;
  let verifyPasswordMock: jest.Mock<VerifyPassword>;
  let bridge: Pick<StartupBridgeService, 'verifyPassword'>;
  let authState: WritableSignal<AuthState | null>;
  let startupStore: { authState: WritableSignal<AuthState | null> };
  let inputMarbles: string;
  let inputValues: Record<string, string>;
  let responseMarbles: string;
  let responseValues: Record<string, boolean>;

  beforeEach((): void => {
    scheduler = new TestScheduler((actual: unknown, expected: unknown): void => {
      expect(actual).toEqual(expected);
    });

    store = signalState<UnlockState>({...initialState});
    authState = signal<AuthState | null>('scrypt');
    startupStore = {authState};

    verifyPasswordMock = jest.fn<VerifyPassword>();
    bridge = {verifyPassword: verifyPasswordMock};

    inputMarbles = 'a';
    inputValues = {a: 'hunter2'};
    responseMarbles = '(v|)';
    responseValues = {v: true};
  });

  function run(): void {
    scheduler.run(({cold, hot}: RunHelpers): void => {
      verifyPasswordMock.mockReturnValue(cold(responseMarbles, responseValues));
      const source$: HotObservable<string> = hot<string>(inputMarbles, inputValues);
      verifyPasswordPipe(store, bridge, startupStore)(source$).subscribe();
    });
  }

  it('patches passwordMatches to true on a matching answer', (): void => {
    run();

    expect(getState(store)).toEqual({...initialState, passwordMatches: true});
  });

  it('debounces a burst of input to one bridge call carrying the last value', (): void => {
    inputMarbles = `a-----b`;
    inputValues = {a: 'first', b: 'second'};

    run();

    expect(verifyPasswordMock).toHaveBeenCalledTimes(1);
    expect(verifyPasswordMock).toHaveBeenCalledWith('second');
  });

  describe('when passwordMatches was already true', (): void => {
    beforeEach((): void => {
      store = signalState<UnlockState>({...initialState, passwordMatches: true});
    });

    it('patches passwordMatches to false on a non-matching answer', (): void => {
      responseValues = {v: false};

      run();

      expect(getState(store)).toEqual({...initialState, passwordMatches: false});
    });

    it('patches passwordMatches to false when the bridge call is rejected', (): void => {
      responseMarbles = '#';

      run();

      expect(getState(store)).toEqual({...initialState, passwordMatches: false});
    });
  });

  describe('when the auth state is pending', (): void => {
    beforeEach((): void => {
      authState.set('pending');
    });

    it('makes no bridge call at all and leaves passwordMatches untouched', (): void => {
      run();

      expect(verifyPasswordMock).not.toHaveBeenCalled();
      expect(getState(store)).toEqual(initialState);
    });
  });
});
