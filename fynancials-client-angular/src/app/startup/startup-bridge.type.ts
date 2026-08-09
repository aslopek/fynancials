export type StartupMode = 'boot' | 'configure' | 'unlock';
export type AuthState = 'passwordless' | 'pending' | 'scrypt';

export type StartupState = {
  authState: AuthState | null
  databasePath: string | null
  mode: StartupMode
};

export type BackendStartOutcome = {
  reachable: boolean
  startedFrom: AuthState
};

/**
 * 'pending' is the only  AuthState where the app does not know anything about the database.
 */
export type KnownAuthState = Exclude<AuthState, 'pending'>;

export type KnownDatabase = {
  path: string
  authState: KnownAuthState
};

/** Mirrors the main process's `ConfigFileState` */
export type ConfigFileState = 'read' | 'missing' | 'unreadable';

export type ConfigureState = {
  configFileState: ConfigFileState
  knownDatabases: KnownDatabase[]
  logPath: string
};

export type PickedDatabase = {
  basePath: string
  fileExists: boolean
};

export type ConfigurationChanges = {
  databasePath: string
};

/**
 * `authState` stays the full `AuthState`: `config:apply` reports the state of whatever database was just selected,
 * and a freshly created one is legitimately `pending`. The narrowing belongs to `KnownDatabase` alone.
 */
export type AppliedConfiguration = {
  databasePath: string
  authState: AuthState
};

export type FynancialsBridge = {
  getStartupState: () => Promise<StartupState>
  startBackend: (password: string) => Promise<BackendStartOutcome>
  verifyPassword: (password: string) => Promise<boolean>
  getConfigureState: () => Promise<ConfigureState>
  pickExistingDatabase: (currentSelection: string | null) => Promise<string | null>
  pickNewDatabase: (currentSelection: string | null) => Promise<PickedDatabase | null>
  forgetPassword: (databasePath: string) => Promise<void>
  applyConfiguration: (changes: ConfigurationChanges) => Promise<AppliedConfiguration>
  quit: () => void
};
