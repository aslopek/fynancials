import {beforeEach, describe, expect, it} from '@jest/globals';
import {signal, Signal, WritableSignal} from '@angular/core';
import {databaseFileName} from './database-file-name';

describe('databaseFileName', (): void => {
  let databasePath: WritableSignal<string | null>;

  beforeEach((): void => {
    databasePath = signal<string | null>('C:\\Users\\x\\fynancials');
  });

  it('takes the last segment of a Windows path', (): void => {
    const result: Signal<string> = databaseFileName({databasePath});

    expect(result()).toBe('fynancials');
  });

  it('takes the last segment of a POSIX path', (): void => {
    databasePath.set('/home/x/fynancials');

    const result: Signal<string> = databaseFileName({databasePath});

    expect(result()).toBe('fynancials');
  });

  it('returns a bare name with no separator unchanged', (): void => {
    databasePath.set('fynancials');

    const result: Signal<string> = databaseFileName({databasePath});

    expect(result()).toBe('fynancials');
  });

  it('returns an empty string when the database path is null', (): void => {
    databasePath.set(null);

    const result: Signal<string> = databaseFileName({databasePath});

    expect(result()).toBe('');
  });
});
