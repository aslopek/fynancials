import {beforeEach, describe, expect, it} from '@jest/globals';
import {signal, Signal, WritableSignal} from '@angular/core';
import {enableSaveAndStart} from './enable-save-and-start';

describe('enableSaveAndStart', (): void => {
  let databaseValid: WritableSignal<boolean>;
  let javaValid: WritableSignal<boolean>;

  beforeEach((): void => {
    databaseValid = signal<boolean>(true);
    javaValid = signal<boolean>(true);
  });

  it('is enabled while every section reports itself valid', (): void => {
    const result: Signal<boolean> = enableSaveAndStart(databaseValid, javaValid);

    expect(result()).toBe(true);
  });

  it('is disabled while the database section reports itself incomplete', (): void => {
    databaseValid.set(false);

    const result: Signal<boolean> = enableSaveAndStart(databaseValid, javaValid);

    expect(result()).toBe(false);
  });

  it('is disabled while the java section reports itself incomplete', (): void => {
    javaValid.set(false);

    const result: Signal<boolean> = enableSaveAndStart(databaseValid, javaValid);

    expect(result()).toBe(false);
  });
});
