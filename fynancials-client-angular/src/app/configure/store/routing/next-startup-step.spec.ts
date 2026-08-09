import {describe, expect, it} from '@jest/globals';
import {nextStartupStep} from './next-startup-step';

describe('nextStartupStep', (): void => {
  it('starts the backend with the defined password for a created database', (): void => {
    expect(nextStartupStep('created', 'pending', 'hunter2')).toEqual({action: 'start', password: 'hunter2'});
  });

  it('starts the backend with the empty password for a created database left without one', (): void => {
    expect(nextStartupStep('created', 'pending', '')).toEqual({action: 'start', password: ''});
  });

  it('hands a picked, pending database to the unlock screen', (): void => {
    expect(nextStartupStep('picked', 'pending')).toEqual({action: 'unlock'});
  });

  it('hands an unchanged, pending database to the unlock screen', (): void => {
    expect(nextStartupStep('unchanged', 'pending')).toEqual({action: 'unlock'});
  });

  it('hands a database with a stored record to the unlock screen', (): void => {
    expect(nextStartupStep('known', 'scrypt')).toEqual({action: 'unlock'});
  });

  it('starts the backend with the empty password for a passwordless database', (): void => {
    expect(nextStartupStep('known', 'passwordless')).toEqual({action: 'start', password: ''});
  });

  it('ignores a typed password for a passwordless database that was not created here', (): void => {
    expect(nextStartupStep('unchanged', 'passwordless', 'hunter2')).toEqual({action: 'start', password: ''});
  });
});
