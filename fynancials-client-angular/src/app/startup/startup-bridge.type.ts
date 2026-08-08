export type StartupMode = 'boot' | 'configure' | 'unlock';
export type AuthState = 'passwordless' | 'pending' | 'scrypt';

export type StartupState = {
  databasePath: string | null
  mode: StartupMode
};

export type BackendStartOutcome = {
  reachable: boolean
  startedFrom: AuthState
};

export type FynancialsBridge = {
  getStartupState: () => Promise<StartupState>
  startBackend: (password: string) => Promise<BackendStartOutcome>
};
