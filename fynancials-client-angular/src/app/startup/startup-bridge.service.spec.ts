import {beforeEach, describe, expect, it, jest} from '@jest/globals';
import {firstValueFrom} from 'rxjs';
import {BridgeHost} from './bridge-host.token';
import {StartupBridgeService} from './startup-bridge.service';
import {
  AppliedConfiguration,
  BackendStartOutcome,
  ConfigurationChanges,
  ConfigureState,
  FynancialsBridge,
  PickedDatabase,
  StartupState
} from './startup-bridge.type';

type GetStartupState = () => Promise<StartupState>;
type StartBackend = (password: string) => Promise<BackendStartOutcome>;
type VerifyPassword = (password: string) => Promise<boolean>;
type GetConfigureState = () => Promise<ConfigureState>;
type PickExistingDatabase = (currentSelection: string | null) => Promise<string | null>;
type PickNewDatabase = (currentSelection: string | null) => Promise<PickedDatabase | null>;
type ForgetPassword = (databasePath: string) => Promise<void>;
type ApplyConfiguration = (changes: ConfigurationChanges) => Promise<AppliedConfiguration>;

describe('StartupBridgeService', (): void => {
  const databasePath: string = 'C:\\Users\\x\\fynancials';

  let startupState: StartupState;
  let startOutcome: BackendStartOutcome;
  let configureState: ConfigureState;
  let pickedDatabase: PickedDatabase;
  let appliedConfiguration: AppliedConfiguration;
  let getStartupState: jest.Mock<GetStartupState>;
  let startBackend: jest.Mock<StartBackend>;
  let verifyPassword: jest.Mock<VerifyPassword>;
  let getConfigureState: jest.Mock<GetConfigureState>;
  let pickExistingDatabase: jest.Mock<PickExistingDatabase>;
  let pickNewDatabase: jest.Mock<PickNewDatabase>;
  let forgetPassword: jest.Mock<ForgetPassword>;
  let applyConfiguration: jest.Mock<ApplyConfiguration>;
  let quit: jest.Mock<() => void>;
  let service: StartupBridgeService;

  beforeEach((): void => {
    startupState = {authState: 'scrypt', databasePath, mode: 'unlock'};
    startOutcome = {reachable: true, startedFrom: 'pending'};
    configureState = {
      configFileState: 'read',
      knownDatabases: [
        {
          path: databasePath,
          authState: 'scrypt'
        }
      ],
      logPath: 'C:\\apps\\fynancials\\fynancials.log'
    };
    pickedDatabase = {basePath: 'D:\\backup\\fynancials-test', fileExists: false};
    appliedConfiguration = {databasePath, authState: 'passwordless'};

    getStartupState = jest.fn<GetStartupState>(() => Promise.resolve(startupState));
    startBackend = jest.fn<StartBackend>(() => Promise.resolve(startOutcome));
    verifyPassword = jest.fn<VerifyPassword>(() => Promise.resolve(true));
    getConfigureState = jest.fn<GetConfigureState>(() => Promise.resolve(configureState));
    pickExistingDatabase = jest.fn<PickExistingDatabase>(() => Promise.resolve(pickedDatabase.basePath));
    pickNewDatabase = jest.fn<PickNewDatabase>(() => Promise.resolve(pickedDatabase));
    forgetPassword = jest.fn<ForgetPassword>(() => Promise.resolve());
    applyConfiguration = jest.fn<ApplyConfiguration>(() => Promise.resolve(appliedConfiguration));
    quit = jest.fn<() => void>();

    const fynancials: FynancialsBridge = {
      getStartupState,
      startBackend,
      verifyPassword,
      getConfigureState,
      pickExistingDatabase,
      pickNewDatabase,
      forgetPassword,
      applyConfiguration,
      quit
    };
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

  it('does not call the bridge before subscription for getConfigureState', (): void => {
    service.getConfigureState();

    expect(getConfigureState).not.toHaveBeenCalled();
  });

  it('resolves the configure state through the bridge', async (): Promise<void> => {
    await expect(firstValueFrom(service.getConfigureState())).resolves.toBe(configureState);
    expect(getConfigureState).toHaveBeenCalledTimes(1);
  });

  it('does not call the bridge before subscription for pickExistingDatabase', (): void => {
    service.pickExistingDatabase(databasePath);

    expect(pickExistingDatabase).not.toHaveBeenCalled();
  });

  it('picks an existing database through the bridge with the current selection', async (): Promise<void> => {
    const picked: string | null = await firstValueFrom(service.pickExistingDatabase(databasePath));

    expect(pickExistingDatabase).toHaveBeenCalledTimes(1);
    expect(pickExistingDatabase).toHaveBeenCalledWith(databasePath);
    expect(picked).toBe(pickedDatabase.basePath);
  });

  it('passes a cancelled open dialog through', async (): Promise<void> => {
    pickExistingDatabase.mockReturnValue(Promise.resolve(null));

    await expect(firstValueFrom(service.pickExistingDatabase(null))).resolves.toBeNull();
    expect(pickExistingDatabase).toHaveBeenCalledTimes(1);
    expect(pickExistingDatabase).toHaveBeenCalledWith(null);
  });

  it('does not call the bridge before subscription for pickNewDatabase', (): void => {
    service.pickNewDatabase(databasePath);

    expect(pickNewDatabase).not.toHaveBeenCalled();
  });

  it('picks a new database through the bridge with the current selection', async (): Promise<void> => {
    const picked: PickedDatabase | null = await firstValueFrom(service.pickNewDatabase(databasePath));

    expect(pickNewDatabase).toHaveBeenCalledTimes(1);
    expect(pickNewDatabase).toHaveBeenCalledWith(databasePath);
    expect(picked).toBe(pickedDatabase);
  });

  it('passes a cancelled save dialog through', async (): Promise<void> => {
    pickNewDatabase.mockReturnValue(Promise.resolve(null));

    await expect(firstValueFrom(service.pickNewDatabase(null))).resolves.toBeNull();
    expect(pickNewDatabase).toHaveBeenCalledTimes(1);
    expect(pickNewDatabase).toHaveBeenCalledWith(null);
  });

  it('does not call the bridge before subscription for forgetPassword', (): void => {
    service.forgetPassword(databasePath);

    expect(forgetPassword).not.toHaveBeenCalled();
  });

  it('forgets a password through the bridge for the given database', async (): Promise<void> => {
    await firstValueFrom(service.forgetPassword(databasePath));

    expect(forgetPassword).toHaveBeenCalledTimes(1);
    expect(forgetPassword).toHaveBeenCalledWith(databasePath);
  });

  it('does not call the bridge before subscription for applyConfiguration', (): void => {
    service.applyConfiguration({databasePath});

    expect(applyConfiguration).not.toHaveBeenCalled();
  });

  it('applies the configuration through the bridge and emits what was applied', async (): Promise<void> => {
    const applied: AppliedConfiguration = await firstValueFrom(service.applyConfiguration({databasePath}));

    expect(applyConfiguration).toHaveBeenCalledTimes(1);
    expect(applyConfiguration).toHaveBeenCalledWith({databasePath});
    expect(applied).toBe(appliedConfiguration);
  });

  it('reaches the bridge eagerly for quit, with no subscription involved', (): void => {
    service.quit();

    expect(quit).toHaveBeenCalledTimes(1);
  });

  describe('without a bridge', (): void => {
    beforeEach((): void => {
      service = new StartupBridgeService({});
    });

    it.each([
      ['getConfigureState', (): unknown => firstValueFrom(service.getConfigureState())],
      ['pickExistingDatabase', (): unknown => firstValueFrom(service.pickExistingDatabase(null))],
      ['pickNewDatabase', (): unknown => firstValueFrom(service.pickNewDatabase(null))],
      ['forgetPassword', (): unknown => firstValueFrom(service.forgetPassword(databasePath))],
      ['applyConfiguration', (): unknown => firstValueFrom(service.applyConfiguration({databasePath}))]
    ])('rejects %s', async (_name: string, call: () => unknown): Promise<void> => {
      await expect(call()).rejects.toThrow('The fynancials bridge is not available');
    });
  });
});
