import {beforeEach, describe, expect, it} from '@jest/globals';
import {signal, Signal, WritableSignal} from '@angular/core';
import {SelectionOrigin} from '../../routing/next-startup-step';
import {databaseValid} from './database-valid';

describe('databaseValid', (): void => {
  const databasePath: string = 'C:\\Users\\x\\traquity';

  let password: WritableSignal<string>;
  let passwordConfirmation: WritableSignal<string>;
  let selectedDatabasePath: WritableSignal<string | null>;
  let selectionOrigin: WritableSignal<SelectionOrigin>;

  beforeEach((): void => {
    password = signal<string>('');
    passwordConfirmation = signal<string>('');
    selectedDatabasePath = signal<string | null>(null);
    selectionOrigin = signal<SelectionOrigin>('unchanged');
  });

  function result(): Signal<boolean> {
    return databaseValid({password, passwordConfirmation, selectedDatabasePath, selectionOrigin});
  }

  it('is invalid while no database is selected', (): void => {
    expect(result()()).toBe(false);
  });

  it('is valid for a selection carried over unchanged', (): void => {
    selectedDatabasePath.set(databasePath);

    expect(result()()).toBe(true);
  });

  it('is valid for a picked database, which shows no password inputs at all', (): void => {
    selectedDatabasePath.set(databasePath);
    selectionOrigin.set('picked');

    expect(result()()).toBe(true);
  });

  describe('for a database created here', (): void => {
    beforeEach((): void => {
      selectedDatabasePath.set(databasePath);
      selectionOrigin.set('created');
    });

    it('is valid while both password inputs are empty', (): void => {
      expect(result()()).toBe(true);
    });

    it('is valid when the confirmation matches the password', (): void => {
      password.set('hunter2');
      passwordConfirmation.set('hunter2');

      expect(result()()).toBe(true);
    });

    it('is invalid when the confirmation differs from the password', (): void => {
      password.set('hunter2');

      expect(result()()).toBe(false);
    });
  });
});
