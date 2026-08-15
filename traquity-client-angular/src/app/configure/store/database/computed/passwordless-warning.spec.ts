import {beforeEach, describe, expect, it} from '@jest/globals';
import {signal, Signal, WritableSignal} from '@angular/core';
import {passwordlessWarning} from './passwordless-warning';

describe('passwordlessWarning', (): void => {
  let password: WritableSignal<string>;
  let passwordConfirmation: WritableSignal<string>;
  let definesPassword: WritableSignal<boolean>;

  beforeEach((): void => {
    password = signal<string>('');
    passwordConfirmation = signal<string>('');
    definesPassword = signal<boolean>(true);
  });

  it('warns while both inputs are empty', (): void => {
    const result: Signal<boolean> = passwordlessWarning({password, passwordConfirmation}, definesPassword);

    expect(result()).toBe(true);
  });

  it('stops warning as soon as a password is typed', (): void => {
    password.set('hunter2');

    const result: Signal<boolean> = passwordlessWarning({password, passwordConfirmation}, definesPassword);

    expect(result()).toBe(false);
  });

  it('stops warning as soon as the confirmation is typed', (): void => {
    passwordConfirmation.set('hunter2');

    const result: Signal<boolean> = passwordlessWarning({password, passwordConfirmation}, definesPassword);

    expect(result()).toBe(false);
  });

  it('warns about nothing when no password is being defined', (): void => {
    definesPassword.set(false);

    const result: Signal<boolean> = passwordlessWarning({password, passwordConfirmation}, definesPassword);

    expect(result()).toBe(false);
  });
});
