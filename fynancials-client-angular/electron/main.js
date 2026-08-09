const {app, dialog, ipcMain} = require('electron');
const spawn = require('child_process').spawn;
const spawnSync = require('child_process').spawnSync;
const path = require('path');
const fs = require('fs');
const os = require('os');
const {Readable} = require('stream');
const {pipeline} = require('stream/promises');
const {createConfigFile} = require('./config/config-file.js');
const {createAuthRegistry} = require('./config/auth-registry.js');
const {BACKEND_PID_URL, createBackendReachability} = require('./backend/backend-reachable.js');
const {createBackendProcess} = require('./backend/backend-process.js');
const {createStartupMode} = require('./window/startup-mode.js');
const {createStartupBridge} = require('./ipc/startup-bridge.js');
const {createMainWindow} = require('./window/main-window.js');

const title = 'Fynancials';
const askForJavaDownload = 'Java not found. Do you want to download Amazon Corretto 25?';
const javaDownloadLicenseNote = 'Amazon Corretto is licensed under the GPLv2 with the Classpath Exception. '
  + 'The license terms are included in the downloaded archive. See https://aws.amazon.com/corretto/faqs/ for details.';

if (process.platform === 'darwin') {
  process.chdir(path.resolve(process.argv0, '..', '..', '..', '..'));
}

const resourcesDir = app.isPackaged ? process.resourcesPath : path.join(__dirname, '..', 'resources');
const backendPath = path.join(resourcesDir, 'backend.jar');

const logPath = path.join(process.cwd(), 'fynancials.log');

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
const startupMode = createStartupMode({configFile, config, authRegistry});
const backendProcess = createBackendProcess({
  spawn: spawnChildProcess,
  resolveJava: verifyJava,
  backendPath,
  config,
  authRegistry,
  backendReachability,
  logFileSystem: {createWriteStream: fs.createWriteStream},
  logPath
});

/**
 * A thin wrapper around `child_process.spawn`, typed to exactly the three-argument call `backendProcess` makes - `spawn`
 * itself is overloaded across many stdio configurations, and assigning the bare function to `BackendProcessOptions.spawn`
 * cannot pick the right overload, while a normal call expression like this one resolves it the same way it always has.
 *
 * @param {string} command
 * @param {string[]} args
 * @param {{env: NodeJS.ProcessEnv}} spawnOptions
 * @returns {import('./backend/backend-process.js').SpawnedBackendProcess}
 */
function spawnChildProcess(command, args, spawnOptions) {
  return spawn(command, args, spawnOptions);
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

app.on('ready', () => {
  removePreviousLog();
  const startupState = startupMode.resolve();
  createStartupBridge({
    ipcMain,
    startupState,
    backendProcess,
    authRegistry,
    config,
    quit: () => app.quit()
  }).register();
  createMainWindow();
});

app.on('window-all-closed', () => {
  backendProcess.kill();
  app.quit();
  process.exit(0);
});
