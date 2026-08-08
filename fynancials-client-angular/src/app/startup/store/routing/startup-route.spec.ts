import {describe, expect, it} from '@jest/globals';
import {phaseAfterFailedStart, startupRouteFor} from './startup-route';

describe('startupRouteFor', (): void => {
  it('returns null for the booting phase', (): void => {
    expect(startupRouteFor('booting')).toBeNull();
  });

  it('returns /unlock for the unlock phase', (): void => {
    expect(startupRouteFor('unlock')).toBe('/unlock');
  });

  it('returns /configure for the configure phase', (): void => {
    expect(startupRouteFor('configure')).toBe('/configure');
  });
});

describe('phaseAfterFailedStart', (): void => {
  it('returns unlock for a pending database', (): void => {
    expect(phaseAfterFailedStart('pending')).toBe('unlock');
  });

  it('returns configure for a scrypt database', (): void => {
    expect(phaseAfterFailedStart('scrypt')).toBe('configure');
  });

  it('returns configure for a passwordless database', (): void => {
    expect(phaseAfterFailedStart('passwordless')).toBe('configure');
  });
});
