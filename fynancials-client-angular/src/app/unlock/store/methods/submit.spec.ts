import {beforeEach, describe, expect, it, jest} from '@jest/globals';
import {signal, WritableSignal} from '@angular/core';
import {submit} from './submit';

type StartBackend = (password: string) => void;

describe('submit', (): void => {
  let password: WritableSignal<string>;
  let canSubmit: WritableSignal<boolean>;
  let startBackend: jest.Mock<StartBackend>;

  beforeEach((): void => {
    password = signal<string>('hunter2');
    canSubmit = signal<boolean>(true);
    startBackend = jest.fn<StartBackend>();
  });

  it('starts the backend with the password when canSubmit is true', (): void => {
    submit({password, canSubmit}, {startBackend});

    expect(startBackend).toHaveBeenCalledTimes(1);
    expect(startBackend).toHaveBeenCalledWith('hunter2');
  });

  describe('when canSubmit is false', (): void => {
    beforeEach((): void => {
      canSubmit.set(false);
    });

    it('does not start the backend', (): void => {
      submit({password, canSubmit}, {startBackend});

      expect(startBackend).not.toHaveBeenCalled();
    });
  });
});
