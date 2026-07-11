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

const frontendUrlPath = path.join(__dirname, 'dist', 'fynancials', 'browser', 'index.html');
const frontendIconPath = path.join(__dirname, 'dist', 'fynancials', 'browser', 'favicon.ico');
const title = 'Fynancials';
const askForJavaDownload = 'Java not found. Do you want to download Amazon Corretto 25?';
const javaDownloadLicenseNote = 'Amazon Corretto is licensed under the GPLv2 with the Classpath Exception. '
  + 'The license terms are included in the downloaded archive. See https://aws.amazon.com/corretto/faqs/ for details.';
let backendPath = '';
let prompt;

if (process.platform === 'darwin') {
  process.chdir(path.resolve(process.argv0, '..', '..', '..', '..'));
}

const resourcesDir = app.isPackaged ? process.resourcesPath : path.join(__dirname, 'resources');
backendPath = path.join(resourcesDir, 'backend.jar');
prompt = require(path.join(resourcesDir, 'node_modules', 'custom-electron-prompt'));

const logPath = path.join(process.cwd(), 'fynancials.log');

/** @type Electron.CrossProcessExports.BrowserWindow  */
let frontend;

/** @type ChildProcessWithoutNullStreams */
let backend;

/**
 * @typedef {Object} FynancialsConfig
 * @property {Object.<string, string>} env - environment variables
 * @property {Object.<string, boolean>} askForPassword - define whether to ask for password when opening the file
 */
/** @type FynancialsConfig */
let config;
const pathToConfigFile = path.join(os.homedir(), 'fynancials.config.json');

function saveConfig() {
  try {
    fs.writeFileSync(pathToConfigFile, JSON.stringify(config, null, 2), {
      flag: 'w'
    });
  } catch (error) {
    console.error(`Failed to save config to ${pathToConfigFile}:`, error);
  }
}

function defaultConfig() {
  return {
    env: {
      FY_DB_FILE_PATH: path.join(os.homedir(), 'fynancials')
    },
    askForPassword: {}
  };
}

function loadConfig() {
  if (!fs.existsSync(pathToConfigFile)) {
    config = defaultConfig();
    saveConfig();
    return;
  }

  try {
    config = JSON.parse(fs.readFileSync(pathToConfigFile, 'utf-8'));
  } catch (error) {
    // corrupt or unreadable config file - fall back to defaults rather than crashing the app on startup
    console.error(`Failed to read config from ${pathToConfigFile}, falling back to defaults:`, error);
    config = defaultConfig();
    saveConfig();
  }
}

async function promptPassword() {
  const databaseFileLocation = config.env.FY_DB_FILE_PATH;
  const fileLocationSpecified = databaseFileLocation != null;
  const doNotAskForPassword = fileLocationSpecified && config.askForPassword[databaseFileLocation] === false;

  if (doNotAskForPassword) {
    return new Promise(resolve => resolve(''));
  }

  return new Promise(resolve => {
    prompt({
      title: 'Password',
      label: 'Enter the password',
      customStylesheet: 'dark',
      inputAttrs: {
        type: 'password'
      }
    }).catch(error => showErrorMessage(error)).then(result => {
      if (result === null) {
        process.exit(0);
      }

      if (config.askForPassword[databaseFileLocation] == null) {
        config.askForPassword[databaseFileLocation] = result !== '';
        saveConfig();
      }

      resolve(result);
    })
  });
}

function showErrorMessage(message) {
  dialog.showMessageBoxSync({
    type: 'error',
    title: 'Fynancials',
    message: message ?? 'An error has occurred',
    buttons: ['OK']
  });
  if (process.platform !== 'darwin') {
    app.quit();
  }
  process.exit(1);
}

/**
 * Runs an external command without a shell (no string interpolation into a shell command line, so paths/args with spaces or
 * special characters can't break or inject into the invocation). Throws on a non-zero exit or a spawn failure, mirroring
 * execSync's throw-on-failure behavior so callers can use try/catch.
 */
function runSync(command, args) {
  const result = spawnSync(command, args);
  if (result.error) {
    throw result.error;
  }
  if (result.status !== 0) {
    throw new Error(`${command} ${args.join(' ')} exited with code ${result.status}`);
  }
}

async function downloadFile(fileUrl, destinationPath) {
  const response = await fetch(fileUrl);
  if (!response.ok || response.body == null) {
    throw new Error(`Failed to download ${fileUrl}: HTTP ${response.status}`);
  }
  await pipeline(Readable.fromWeb(response.body), fs.createWriteStream(destinationPath));
}

function resolveTarPath() {
  if (process.platform === 'win32') {
    return path.join(process.env.SystemRoot ?? 'C:\\Windows', 'System32', 'tar.exe');
  }
  return '/usr/bin/tar';
}

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
      showErrorMessage(error);
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

async function startBackend(java) {
  if (backend != null) {
    return;
  }
  try {
    if (fs.existsSync(logPath)) {
      fs.unlinkSync(logPath);
    }
  } catch (error) {
    // non-fatal - worst case the previous run's log lines stay at the top since we open in append mode below
    console.error(`Failed to remove previous log file at ${logPath}:`, error);
  }

  const password = await promptPassword();
  backend = spawn(java, ['-jar', backendPath], {
    env: {
      ...process.env,
      ...config.env,
      FY_DB_FILE_PASSWORD: password
    }
  });

  try {
    const logStream = fs.createWriteStream(logPath, {flags: 'a'});
    logStream.on('error', (error) => console.error(`Failed to write backend log to ${logPath}:`, error));
    backend.stdout.pipe(logStream, {end: false});
    backend.stderr.pipe(logStream, {end: false});
    backend.on('close', () => logStream.end());
  } catch (error) {
    console.error(`Failed to set up backend logging at ${logPath}:`, error);
  }
}

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

async function startApplication() {
  loadConfig();
  const java = await verifyJava();
  startBackend(java);
  startFrontend();
}

app.on('ready', startApplication);
app.on('window-all-closed', () => {
  if (backend != null) {
    backend.kill('SIGTERM');
    backend = null;
  }

  if (process.platform !== 'darwin') {
    app.quit();
  }
  process.exit(0);
});

app.on('activate', () => {
  if (frontend == null) {
    startFrontend();
  }
});