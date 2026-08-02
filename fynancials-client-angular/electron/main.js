const {app, BrowserWindow, screen, shell} = require('electron');
const spawn = require('child_process').spawn;
const spawnSync = require('child_process').spawnSync;
const path = require('path');
const fs = require('fs');
const os = require('os');
const url = require('url');
const {Readable} = require('stream');
const {pipeline} = require('stream/promises');
const {dialog} = require('electron');
const {createConfigFile} = require('./config/config-file.js');
const {createAuthRegistry} = require('./config/auth-registry.js');
const {BACKEND_PID_URL, createBackendReachability} = require('./backend/backend-reachable.js');

/** @import {AuthState} from './config/auth.js' */

// paths here are relative to electron/, so '..' reaches the package root
const frontendUrlPath = path.join(__dirname, '..', 'dist', 'fynancials', 'browser', 'index.html');
const frontendIconPath = path.join(__dirname, '..', 'dist', 'fynancials', 'browser', 'favicon.ico');
const title = 'Fynancials';
const askForJavaDownload = 'Java not found. Do you want to download Amazon Corretto 25?';
const javaDownloadLicenseNote = 'Amazon Corretto is licensed under the GPLv2 with the Classpath Exception. '
  + 'The license terms are included in the downloaded archive. See https://aws.amazon.com/corretto/faqs/ for details.';
const wrongPasswordMessage = 'Wrong password for the configured database. Please try again.';

if (process.platform === 'darwin') {
  process.chdir(path.resolve(process.argv0, '..', '..', '..', '..'));
}

const resourcesDir = app.isPackaged ? process.resourcesPath : path.join(__dirname, '..', 'resources');
const backendPath = path.join(resourcesDir, 'backend.jar');
// resolved dynamically, so its type has to be asserted; the return type is what matters here, since the prompt's
// result flows on as the database password
const prompt = /** @type {(options: object) => Promise<string | null>} */
  (require(path.join(resourcesDir, 'node_modules', 'custom-electron-prompt')));

const logPath = path.join(process.cwd(), 'fynancials.log');

/** @type {import('electron').BrowserWindow | null} */
let frontend = null;

/** @type {import('node:child_process').ChildProcessWithoutNullStreams | null} */
let backend = null;

/**
 * What a start attempt says about the database it was made against. `startedFrom` is the state read *before* the
 * password was asked for, which is what a failed start has to be routed on.
 *
 * @typedef {{reachable: boolean, startedFrom: AuthState}} BackendStartOutcome
 */

/** @type {Promise<BackendStartOutcome> | null} */
let backendStart = null;

const configFile = createConfigFile({
  fileSystem: fs,
  configFilePath: path.join(os.homedir(), 'fynancials.config.json'),
  homeDirectory: os.homedir()
});
const config = configFile.load();
const authRegistry = createAuthRegistry({configFile, config});
const backendReachability = createBackendReachability({
  fetchPid: fetchBackendPid,
  delay
});
/** @type {string | undefined} */
const databasePath = config.env.FY_DB_FILE_PATH;

/**
 * Asks for the database password, unless the database is known to have none. A database with a verified record has
 * the answer checked locally and is asked again on a mismatch, so a wrong password never reaches a backend spawn; a
 * pending one is asked once and started blind, because only the H2 file itself can tell.
 *
 * @param {AuthState} startedFrom
 * @returns {Promise<string>}
 */
async function promptPassword(startedFrom) {
  if (startedFrom === 'passwordless') {
    return '';
  }

  /** @type {string | null} */
  let password;
  while (true) {
    password = await prompt({
      title: 'Password',
      label: 'Enter the password',
      customStylesheet: 'dark',
      inputAttrs: {
        type: 'password'
      }
    }).catch(error => showErrorMessage(error));

    if (password === null) {
      process.exit(0);
    }

    if (startedFrom === 'scrypt' && databasePath != null && !authRegistry.verify(databasePath, password)) {
      showWrongPasswordMessage();
      continue;
    }

    return password;
  }
}

/**
 * @param {string | Error} [message]
 * @returns {never}
 */
function showErrorMessage(message) {
  dialog.showMessageBoxSync({
    type: 'error',
    title: title,
    message: message == null ? 'An error has occurred' : String(message),
    buttons: ['OK']
  });
  app.quit();
  process.exit(1);
}

/**
 * @returns {void}
 */
function showWrongPasswordMessage() {
  dialog.showMessageBoxSync({
    type: 'error',
    title: title,
    message: wrongPasswordMessage,
    buttons: ['OK']
  });
}

/**
 * Runs an external command without a shell (no string interpolation into a shell command line, so paths/args with spaces or
 * special characters can't break or inject into the invocation). Throws on a non-zero exit or a spawn failure, mirroring
 * execSync's throw-on-failure behavior so callers can use try/catch.
 *
 * @param {string} command
 * @param {string[]} args
 * @returns {void}
 */
function runSync(command, args) {
  /** @type {import('node:child_process').SpawnSyncReturns<Buffer<ArrayBuffer>>} */
  const result = spawnSync(command, args);
  if (result.error) {
    throw result.error;
  }
  if (result.status !== 0) {
    throw new Error(`${command} ${args.join(' ')} exited with code ${result.status}`);
  }
}

/**
 * @param {string} fileUrl
 * @param {string} destinationPath
 * @returns {Promise<void>}
 */
async function downloadFile(fileUrl, destinationPath) {
  /** @type {Response} */
  const response = await fetch(fileUrl);
  if (!response.ok || response.body == null) {
    throw new Error(`Failed to download ${fileUrl}: HTTP ${response.status}`);
  }
  await pipeline(Readable.fromWeb(response.body), fs.createWriteStream(destinationPath));
}

/**
 * @returns {string}
 */
function resolveTarPath() {
  if (process.platform === 'win32') {
    return path.join(process.env['SystemRoot'] ?? 'C:\\Windows', 'System32', 'tar.exe');
  }
  return '/usr/bin/tar';
}

/**
 * @returns {Promise<string>}
 */
async function verifyJava() {
  const downloadJavaForWindows = async () => {
    const pathToJava = path.resolve('.', 'java', 'bin', 'java.exe');
    if (fs.existsSync(pathToJava)) {
      return pathToJava;
    }
    const clickedButton = dialog.showMessageBoxSync({
      type: 'question',
      title: title,
      message: askForJavaDownload,
      detail: javaDownloadLicenseNote,
      buttons: ['Yes', 'No']
    });
    if (clickedButton === 1) {
      process.exit(1);
    }

    const file = 'amazon-corretto-25-x64-windows-jdk.zip';
    const url = `https://corretto.aws/downloads/latest/${file}`;
    await downloadFile(url, file);
    runSync(resolveTarPath(), ['-xf', file]);
    fs.rmSync(file);
    fs.readdirSync('.').forEach(directoryEntry => {
      if (directoryEntry.match(/^jdk/)) {
        fs.renameSync(directoryEntry, 'java');
      }
    })
    return pathToJava;
  }

  const downloadJavaForMac = async () => {
    const pathToJava = path.resolve('.', 'java', 'bin', 'java');
    if (fs.existsSync(pathToJava)) {
      return pathToJava;
    }
    const clickedButton = dialog.showMessageBoxSync({
      type: 'question',
      title: title,
      message: askForJavaDownload,
      detail: javaDownloadLicenseNote,
      buttons: ['Yes', 'No']
    });
    if (clickedButton === 1) {
      process.exit(1);
    }

    const file = 'amazon-corretto-25-aarch64-macos-jdk.tar.gz';
    const url = `https://corretto.aws/downloads/latest/${file}`;
    await downloadFile(url, file);
    runSync(resolveTarPath(), ['-xvf', file]);
    fs.rmSync(file, {recursive: true, force: true});

    for (let directoryEntry of fs.readdirSync('.')) {
      if (directoryEntry.match(/^amazon-corretto-[0-9]+\.jdk$/)) {
        fs.renameSync(path.resolve(process.cwd(), directoryEntry, 'Contents', 'Home'), 'java');
        fs.rmSync(directoryEntry, {recursive: true, force: true});
      }
    }
    return pathToJava;
  }

  const downloadJavaForLinux = async () => {
    const javaHome = path.join(os.homedir(), '.fynancials', 'java');
    const pathToJava = path.join(javaHome, 'bin', 'java');
    if (fs.existsSync(pathToJava)) {
      return pathToJava;
    }
    const clickedButton = dialog.showMessageBoxSync({
      type: 'question',
      title: title,
      message: askForJavaDownload,
      detail: javaDownloadLicenseNote,
      buttons: ['Yes', 'No']
    });
    if (clickedButton === 1) {
      process.exit(1);
    }

    const file = 'amazon-corretto-25-x64-linux-jdk.tar.gz';
    const url = `https://corretto.aws/downloads/latest/${file}`;
    const tmpFile = path.join(os.tmpdir(), file);
    await downloadFile(url, tmpFile);

    const fynancialsHome = path.join(os.homedir(), '.fynancials');
    fs.mkdirSync(fynancialsHome, {recursive: true});
    runSync(resolveTarPath(), ['-xf', tmpFile, '-C', fynancialsHome]);
    fs.rmSync(tmpFile);

    fs.readdirSync(fynancialsHome).forEach(directoryEntry => {
      if (directoryEntry.match(/^amazon-corretto-.*-linux-x64$/)) {
        fs.renameSync(path.join(fynancialsHome, directoryEntry), javaHome);
      }
    })
    return pathToJava;
  }

  const downloadJava = async () => {
    try {
      if (process.platform === 'win32' && process.arch === 'x64') {
        return downloadJavaForWindows();
      } else if (process.platform === 'darwin') {
        return downloadJavaForMac();
      } else if (process.platform === 'linux' && process.arch === 'x64') {
        return downloadJavaForLinux();
      } else {
        showErrorMessage(`Java not found on your system. No automatic download available for ${process.platform}/${process.arch}`);
      }
    } catch (error) {
      showErrorMessage(error instanceof Error ? error : String(error));
    }
  };

  return new Promise(resolve => {
    const java = spawn('java', ['-version'], {
      env: {...process.env}
    });
    java.on('error', () => {
      downloadJava().then(pathToJava => resolve(pathToJava));
    });
    java.on('exit', (exitCode) => {
      if (exitCode === 0) {
        resolve('java');
      } else {
        downloadJava().then(pathToJava => resolve(pathToJava));
      }
    });
  });
}

/**
 * Spawns the backend at most once; a repeated call yields the first call's outcome.
 *
 * @param {string} java
 * @returns {Promise<BackendStartOutcome>}
 */
function startBackend(java) {
  backendStart ??= spawnBackend(java);
  return backendStart;
}

/**
 * @param {string} java
 * @returns {Promise<BackendStartOutcome>}
 */
async function spawnBackend(java) {
  removePreviousLog();

  /** @type {AuthState} */
  const startedFrom = databasePath == null ? 'pending' : authRegistry.stateOf(databasePath);
  /** @type {string} */
  const password = await promptPassword(startedFrom);
  /** @type {import('node:child_process').ChildProcessWithoutNullStreams} */
  const backendProcess = spawn(java, ['-jar', backendPath], {
    env: {
      ...process.env,
      ...config.env,
      FY_DB_FILE_PASSWORD: password
    }
  });
  backend = backendProcess;
  pipeBackendLog(backendProcess);

  // a backend that answers proves the H2 file was decrypted with this password - and only then is it recorded
  const reachable = await backendReachability.waitUntilReachable(backendProcess);
  if (reachable && databasePath != null) {
    authRegistry.recordProvenStart(databasePath, password);
  }
  return {reachable, startedFrom};
}

/**
 * @returns {void}
 */
function removePreviousLog() {
  try {
    if (fs.existsSync(logPath)) {
      fs.unlinkSync(logPath);
    }
  } catch (error) {
    // non-fatal - worst case the previous run's log lines stay at the top since we open in append mode below
    console.error(`Failed to remove previous log file at ${logPath}:`, error);
  }
}

/**
 * @param {import('node:child_process').ChildProcessWithoutNullStreams} backendProcess
 * @returns {void}
 */
function pipeBackendLog(backendProcess) {
  try {
    /** @type {import('node:fs').WriteStream} */
    const logStream = fs.createWriteStream(logPath, {flags: 'a'});
    logStream.on('error', (error) => console.error(`Failed to write backend log to ${logPath}:`, error));
    backendProcess.stdout.pipe(logStream, {end: false});
    backendProcess.stderr.pipe(logStream, {end: false});
    backendProcess.on('close', () => logStream.end());
  } catch (error) {
    console.error(`Failed to set up backend logging at ${logPath}:`, error);
  }
}

/**
 * @returns {Promise<boolean>}
 */
async function fetchBackendPid() {
  try {
    /** @type {Response} */
    const response = await fetch(BACKEND_PID_URL);
    return response.status === 200;
  } catch {
    return false;
  }
}

/**
 * @param {number} milliseconds
 * @returns {Promise<void>}
 */
function delay(milliseconds) {
  return new Promise(resolve => {
    setTimeout(resolve, milliseconds);
  });
}

/**
 * @returns {void}
 */
function startFrontend() {
  if (frontend != null) {
    return;
  }

  const {width, height} = screen.getPrimaryDisplay().workAreaSize;
  frontend = new BrowserWindow({
    width: parseInt(`${width * 0.9}`),
    height: parseInt(`${height * 0.9}`),
    center: true,
    icon: frontendIconPath,
    autoHideMenuBar: true,
    webPreferences: {
      devTools: false,
      nodeIntegration: false,
      contextIsolation: true,
      sandbox: true,
      webSecurity: true
    }
  });
  frontend.maximize();

  frontend.loadURL(url.format({
    pathname: frontendUrlPath,
    protocol: 'file:',
    slashes: true
  }));

  frontend.on('closed', () => {
    frontend = null;
  });

  frontend.webContents.setWindowOpenHandler(({url}) => {
    shell.openExternal(url);
    return {action: 'deny'};
  });
}

/**
 * @returns {Promise<void>}
 */
async function startApplication() {
  const java = await verifyJava();
  // deliberately not awaited - the window has to come up while the backend boots. The outcome is the seam the
  // startup-mode routing consumes; nothing acts on it yet, which is why the rejection needs a handler here: an
  // unhandled one terminates the main process, and a start that throws must not take the whole app down with it.
  startBackend(java).catch(error => console.error('Failed to start the backend:', error));
  startFrontend();
}

app.on('ready', startApplication);
app.on('window-all-closed', () => {
  if (backend != null) {
    backend.kill('SIGTERM');
    backend = null;
  }

  app.quit();
  process.exit(0);
});
