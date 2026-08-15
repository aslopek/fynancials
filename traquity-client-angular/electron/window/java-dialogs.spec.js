const {beforeEach, describe, expect, it, jest} = require('@jest/globals');
const {createJavaDialogs} = require('./java-dialogs.js');

/** @import {JavaDialogs, JavaFileSystem, OpenDialogLike} from './java-dialogs.js' */

/**
 * Stands in for the `BrowserWindow` the dialog is parented to. The module hands it straight back to the injected
 * dialog and never reads a member of it, which is exactly why its type is a parameter.
 *
 * @typedef {{label: string}} TestWindow
 */

describe('javaDialogs', () => {
  const currentSetting = 'C:\\jdk\\bin\\java.exe';
  const downloadTarget = 'C:\\apps\\traquity\\java';
  const pickedPath = 'C:\\Program Files\\Java\\bin\\java.exe';

  /** @type {TestWindow} */
  let parentWindow;

  /** @type {TestWindow | null} */
  let window;

  /** @type {JavaDialogs} */
  let dialogs;

  const showOpenDialog = jest.fn(/** @type {OpenDialogLike<TestWindow>['showOpenDialog']} */
    (() => Promise.resolve({canceled: false, filePaths: [pickedPath]})));
  const existsSync = jest.fn(/** @type {JavaFileSystem['existsSync']} */ (() => false));

  beforeEach(() => {
    parentWindow = {label: 'main'};
    jest.clearAllMocks();
    showOpenDialog.mockResolvedValue({canceled: false, filePaths: [pickedPath]});
    existsSync.mockReturnValue(false);
    window = parentWindow;

    /** @type {OpenDialogLike<TestWindow>} */
    const dialog = {showOpenDialog};

    /** @type {JavaFileSystem} */
    const fileSystem = {existsSync};

    dialogs = createJavaDialogs({
      dialog,
      getParentWindow: () => window,
      fileSystem
    });
  });

  it('returns the picked path', async () => {
    await expect(dialogs.pickJavaBinary(currentSetting, downloadTarget)).resolves.toBe(pickedPath);
  });

  it('opens a combined file/directory picker parented to the main window', async () => {
    await dialogs.pickJavaBinary(currentSetting, downloadTarget);

    expect(showOpenDialog).toHaveBeenCalledTimes(1);
    expect(showOpenDialog).toHaveBeenCalledWith(parentWindow, {
      title: 'Select Java runtime',
      properties: ['openFile', 'openDirectory'],
      defaultPath: currentSetting
    });
  });

  it('defaults to the download target when there is no current setting but a runtime is already there', async () => {
    existsSync.mockReturnValue(true);

    await dialogs.pickJavaBinary(null, downloadTarget);

    expect(existsSync).toHaveBeenCalledTimes(1);
    expect(existsSync).toHaveBeenCalledWith(downloadTarget);
    expect(showOpenDialog).toHaveBeenCalledTimes(1);
    expect(showOpenDialog).toHaveBeenCalledWith(parentWindow, {
      title: 'Select Java runtime',
      properties: ['openFile', 'openDirectory'],
      defaultPath: downloadTarget
    });
  });

  it('opens with no default path when there is no current setting and no runtime at the download target', async () => {
    await dialogs.pickJavaBinary(null, downloadTarget);

    expect(showOpenDialog).toHaveBeenCalledTimes(1);
    expect(showOpenDialog).toHaveBeenCalledWith(parentWindow, {
      title: 'Select Java runtime',
      properties: ['openFile', 'openDirectory']
    });
  });

  it('prefers the current setting over the download target', async () => {
    existsSync.mockReturnValue(true);

    await dialogs.pickJavaBinary(currentSetting, downloadTarget);

    expect(existsSync).not.toHaveBeenCalled();
    expect(showOpenDialog).toHaveBeenCalledTimes(1);
    expect(showOpenDialog).toHaveBeenCalledWith(parentWindow, {
      title: 'Select Java runtime',
      properties: ['openFile', 'openDirectory'],
      defaultPath: currentSetting
    });
  });

  it('returns null when the dialog is cancelled', async () => {
    showOpenDialog.mockResolvedValue({canceled: true, filePaths: []});

    await expect(dialogs.pickJavaBinary(currentSetting, downloadTarget)).resolves.toBeNull();
  });

  it('returns null when the dialog yields no path', async () => {
    showOpenDialog.mockResolvedValue({canceled: false, filePaths: []});

    await expect(dialogs.pickJavaBinary(currentSetting, downloadTarget)).resolves.toBeNull();
  });

  it('opens no dialog without a main window', async () => {
    window = null;

    await expect(dialogs.pickJavaBinary(currentSetting, downloadTarget)).resolves.toBeNull();
    expect(showOpenDialog).not.toHaveBeenCalled();
  });
});
