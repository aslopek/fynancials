import {beforeEach, describe, expect, it} from '@jest/globals';
import {signal, Signal, WritableSignal} from '@angular/core';
import {passwordMismatch} from './password-mismatch';

describe('passwordMismatch', (): void => {
  let password: WritableSignal<string>;
  let passwordConfirmation: WritableSignal<string>;
  let definesPassword: WritableSignal<boolean>;

  beforeEach((): void => {
    password = signal<string>('');
    passwordConfirmation = signal<string>('');
    definesPassword = signal<boolean>(true);
  });

  it('reports no mismatch while both inputs are empty', (): void => {
    const result: Signal<boolean> = passwordMismatch({password, passwordConfirmation}, definesPassword);

    expect(result()).toBe(false);
  });

  it('reports a mismatch when the confirmation differs from the password', (): void => {
    password.set('hunter2');

    const result: Signal<boolean> = passwordMismatch({password, passwordConfirmation}, definesPassword);

    expect(result()).toBe(true);
  });

  it('reports no mismatch when the confirmation matches the password', (): void => {
    password.set('hunter2');
    passwordConfirmation.set('hunter2');

    const result: Signal<boolean> = passwordMismatch({password, passwordConfirmation}, definesPassword);

    expect(result()).toBe(false);
  });

  it('reports no mismatch when no password is being defined', (): void => {
    password.set('hunter2');
    definesPassword.set(false);

    const result: Signal<boolean> = passwordMismatch({password, passwordConfirmation}, definesPassword);

    expect(result()).toBe(false);
  });
});
