import {beforeEach, describe, expect, it, jest} from '@jest/globals';
import {firstValueFrom} from 'rxjs';
import {BridgeHost} from './bridge-host.token';
import {StartupBridgeService} from './startup-bridge.service';
import {BackendStartOutcome, FynancialsBridge, StartupState} from './startup-bridge.type';

type GetStartupState = () => Promise<StartupState>;
type StartBackend = (password: string) => Promise<BackendStartOutcome>;
type VerifyPassword = (password: string) => Promise<boolean>;

describe('StartupBridgeService', (): void => {
  let startupState: StartupState;
  let startOutcome: BackendStartOutcome;
  let getStartupState: jest.Mock<GetStartupState>;
  let startBackend: jest.Mock<StartBackend>;
  let verifyPassword: jest.Mock<VerifyPassword>;
  let quit: jest.Mock<() => void>;
  let service: StartupBridgeService;

  beforeEach((): void => {
    startupState = {authState: 'scrypt', databasePath: 'C:\\Users\\x\\fynancials', mode: 'unlock'};
    startOutcome = {reachable: true, startedFrom: 'pending'};
    getStartupState = jest.fn<GetStartupState>(() => Promise.resolve(startupState));
    startBackend = jest.fn<StartBackend>(() => Promise.resolve(startOutcome));
    verifyPassword = jest.fn<VerifyPassword>(() => Promise.resolve(true));
    quit = jest.fn<() => void>();

    const fynancials: FynancialsBridge = {getStartupState, startBackend, verifyPassword, quit};
    const bridgeHost: BridgeHost = {fynancials};
    service = new StartupBridgeService(bridgeHost);
  });

  it('reports the bridge as available', (): void => {
    expect(service.available).toBe(true);
  });

  it('reports the bridge as unavailable when no bridge is present', (): void => {
    service = new StartupBridgeService({});

    expect(service.available).toBe(false);
  });

  it('does not call the bridge before subscription', (): void => {
    service.getStartupState();

    expect(getStartupState).not.toHaveBeenCalled();
  });

  it('resolves the startup state through the bridge', async (): Promise<void> => {
    await expect(firstValueFrom(service.getStartupState())).resolves.toBe(startupState);
  });

  it('starts the backend through the bridge with the given password', async (): Promise<void> => {
    const outcome: BackendStartOutcome = await firstValueFrom(service.startBackend('hunter2'));

    expect(startBackend).toHaveBeenCalledWith('hunter2');
    expect(outcome).toBe(startOutcome);
  });

  it('does not call the bridge before subscription for verifyPassword', (): void => {
    service.verifyPassword('hunter2');

    expect(verifyPassword).not.toHaveBeenCalled();
  });

  it('verifies the password through the bridge and passes a match through', async (): Promise<void> => {
    const matches: boolean = await firstValueFrom(service.verifyPassword('hunter2'));

    expect(verifyPassword).toHaveBeenCalledTimes(1);
    expect(verifyPassword).toHaveBeenCalledWith('hunter2');
    expect(matches).toBe(true);
  });

  it('passes a non-match through', async (): Promise<void> => {
    verifyPassword.mockReturnValue(Promise.resolve(false));

    const matches: boolean = await firstValueFrom(service.verifyPassword('wrong'));

    expect(verifyPassword).toHaveBeenCalledTimes(1);
    expect(verifyPassword).toHaveBeenCalledWith('wrong');
    expect(matches).toBe(false);
  });

  it('reaches the bridge eagerly for quit, with no subscription involved', (): void => {
    service.quit();

    expect(quit).toHaveBeenCalledTimes(1);
  });
});
