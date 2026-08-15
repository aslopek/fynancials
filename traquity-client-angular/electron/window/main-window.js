const {BrowserWindow, screen, shell} = require('electron');
const path = require('path');
const url = require('url');
const {isOpenableExternally, isSameDocument} = require('./navigation-policy.js');

/**
 * Creates the single `BrowserWindow` the packaged app ever opens, always loading the built Angular app. Requires
 * `electron`, so it gets no spec (see `../LLM.md`'s type-safety section) - that is why it holds nothing but the
 * window construction.
 */

// paths here are relative to electron/window/, so two '..' reach the package root
const frontendUrlPath = path.join(__dirname, '..', '..', 'dist', 'traquity', 'browser', 'index.html');
const frontendIconPath = path.join(__dirname, '..', '..', 'dist', 'traquity', 'browser', 'favicon.ico');
const preloadPath = path.join(__dirname, '..', 'preload.js');

/** @type {import('electron').BrowserWindow | null} */
let frontend = null;

/**
 * @returns {void}
 */
function createMainWindow() {
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
      preload: preloadPath,
      devTools: false,
      nodeIntegration: false,
      // a preload in a subframe is a bridge in a subframe, and where a subframe goes is decided by the document's own
      // `frame-src` rather than by the navigation policy below
      nodeIntegrationInSubFrames: false,
      contextIsolation: true,
      sandbox: true,
      webSecurity: true,
      webviewTag: false,
      allowRunningInsecureContent: false,
      experimentalFeatures: false,
      // the renderer displays security names, depot names and file contents a user imported: none of that goes to a
      // spell-check service, and the dictionary downloads that come with it are a fetch this app never asked for
      spellcheck: false
    }
  });
  frontend.maximize();

  // `pathToFileURL` rather than a formatted `file:` URL: it is the canonical, percent-encoded form
  const frontendUrl = url.pathToFileURL(frontendUrlPath).href;
  frontend.loadURL(frontendUrl);

  frontend.on('closed', () => {
    frontend = null;
  });

  // the URL is data - a release page from an HTTP response, a link a user typed - and `openExternal` hands whatever
  // scheme it carries to the OS, so what may not be opened is dropped rather than opened in the window instead
  frontend.webContents.setWindowOpenHandler(({url: requestedUrl}) => {
    if (isOpenableExternally(requestedUrl)) {
      shell.openExternal(requestedUrl);
    }
    return {action: 'deny'};
  });

  // the preload's bridge belongs to this document alone: a window that navigates elsewhere would take every IPC
  // channel along to whatever it lands on
  frontend.webContents.on('will-navigate', (details) => {
    if (!isSameDocument(details.url, frontendUrl)) {
      details.preventDefault();
    }
  });

  // `will-navigate` deliberately stays the top-level rule: the app embeds the backend's H2 console in an iframe, so a
  // `will-frame-navigate` refusing everything but this document would refuse that too. What a subframe may load is
  // the document's own `frame-src` (index.html), and a subframe carries no preload and therefore no bridge.

  // nothing in this app embeds a webview, and one attached later would come with a preload of its own
  frontend.webContents.on('will-attach-webview', (event) => {
    event.preventDefault();
  });

  // a portfolio tracker asks for no camera, no microphone, no location and no notifications, so every request and
  // every check is answered before it can reach a prompt
  frontend.webContents.session.setPermissionRequestHandler((_webContents, _permission, callback) => callback(false));
  frontend.webContents.session.setPermissionCheckHandler(() => false);
  // WebHID/WebUSB/Web Serial are decided by a handler of their own rather than by the two above, and this app pairs
  // with no device at all
  frontend.webContents.session.setDevicePermissionHandler(() => false);
}

/**
 * The window native dialogs are parented to, so none of them can end up behind the app. Null before `createMainWindow`
 * and after the window was closed.
 *
 * @returns {import('electron').BrowserWindow | null}
 */
function getMainWindow() {
  return frontend;
}

module.exports = {createMainWindow, getMainWindow};
