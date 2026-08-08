import {beforeEach, describe, expect, it, jest} from '@jest/globals';
import {firstValueFrom} from 'rxjs';
import {BridgeHost} from './bridge-host.token';
import {StartupBridgeService} from './startup-bridge.service';
import {BackendStartOutcome, FynancialsBridge, StartupState} from './startup-bridge.type';

type GetStartupState = () => Promise<StartupState>;
type StartBackend = (password: string) => Promise<BackendStartOutcome>;

describe('StartupBridgeService', (): void => {
  let startupState: StartupState;
  let startOutcome: BackendStartOutcome;
  let getStartupState: jest.Mock<GetStartupState>;
  let startBackend: jest.Mock<StartBackend>;
  let service: StartupBridgeService;

  beforeEach((): void => {
    startupState = {databasePath: 'C:\\Users\\x\\fynancials', mode: 'unlock'};
    startOutcome = {reachable: true, startedFrom: 'pending'};
    getStartupState = jest.fn<GetStartupState>(() => Promise.resolve(startupState));
    startBackend = jest.fn<StartBackend>(() => Promise.resolve(startOutcome));

    const fynancials: FynancialsBridge = {getStartupState, startBackend};
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
});
