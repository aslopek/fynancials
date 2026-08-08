const {BrowserWindow, screen, shell} = require('electron');
const path = require('path');
const url = require('url');

/**
 * Creates the single `BrowserWindow` the packaged app ever opens, always loading the built Angular app. Requires
 * `electron`, so it gets no spec (see `../LLM.md`'s type-safety section) - that is why it holds nothing but the
 * window construction.
 */

// paths here are relative to electron/window/, so two '..' reach the package root
const frontendUrlPath = path.join(__dirname, '..', '..', 'dist', 'fynancials', 'browser', 'index.html');
const frontendIconPath = path.join(__dirname, '..', '..', 'dist', 'fynancials', 'browser', 'favicon.ico');
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

module.exports = {createMainWindow};
