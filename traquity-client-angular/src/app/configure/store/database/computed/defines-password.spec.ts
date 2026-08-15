import {beforeEach, describe, expect, it} from '@jest/globals';
import {signal, Signal, WritableSignal} from '@angular/core';
import {SelectionOrigin} from '../../routing/next-startup-step';
import {definesPassword} from './defines-password';

describe('definesPassword', (): void => {
  let selectionOrigin: WritableSignal<SelectionOrigin>;

  beforeEach((): void => {
    selectionOrigin = signal<SelectionOrigin>('unchanged');
  });

  it('defines no password for a selection carried over unchanged', (): void => {
    const result: Signal<boolean> = definesPassword({selectionOrigin});

    expect(result()).toBe(false);
  });

  it('defines a password for a database created here', (): void => {
    selectionOrigin.set('created');

    const result: Signal<boolean> = definesPassword({selectionOrigin});

    expect(result()).toBe(true);
  });

  it('defines no password for a picked, existing file', (): void => {
    selectionOrigin.set('picked');

    const result: Signal<boolean> = definesPassword({selectionOrigin});

    expect(result()).toBe(false);
  });

  it('defines no password for a database taken from the known list', (): void => {
    selectionOrigin.set('known');

    const result: Signal<boolean> = definesPassword({selectionOrigin});

    expect(result()).toBe(false);
  });
});
