/**
 * The native picker for a java binary or its containing directory.`dialog` and the
 * main window are injected, so this module is exercisable without an Electron instance, and no dialog opens without
 * a parent window to attach to and return a selection to.
 *
 * The raw picked path is all this module reports - normalizing a picked directory to a binary and verifying it
 * belong to the bridge handler that composes dialog, normalization and verification together.
 */

/**
 * Electron's `dialog`, narrowed to the one call made here.
 *
 * @template TWindow
 * @typedef {Object} OpenDialogLike
 * @property {(window: TWindow, options: import('electron').OpenDialogOptions) =>
 *   Promise<import('electron').OpenDialogReturnValue>} showOpenDialog
 */

/**
 * @typedef {Object} JavaFileSystem
 * @property {(path: string) => boolean} existsSync
 */

/**
 * @typedef {Object} JavaDialogs
 * @property {(currentSetting: string | null, downloadTarget: string) => Promise<string | null>} pickJavaBinary
 */

/**
 * @template TWindow
 * @typedef {Object} JavaDialogsOptions
 * @property {OpenDialogLike<TWindow>} dialog
 * @property {() => TWindow | null} getParentWindow the window a dialog is parented to; without one there is nothing
 *   to parent to and nothing to return a selection to, so no dialog opens
 * @property {JavaFileSystem} fileSystem
 */

/**
 * @template TWindow
 * @param {JavaDialogsOptions<TWindow>} options
 * @returns {JavaDialogs}
 */
function createJavaDialogs(options) {
  const {dialog, getParentWindow, fileSystem} = options;

  /**
   * The current setting wins as the dialog's starting point; with none, the download target is offered only when a
   * runtime is already there to point at.
   *
   * @param {string | null} currentSetting
   * @param {string} downloadTarget
   * @returns {{defaultPath?: string}}
   */
  function defaultPathOf(currentSetting, downloadTarget) {
    if (currentSetting != null) {
      return {defaultPath: currentSetting};
    }
    return fileSystem.existsSync(downloadTarget) ? {defaultPath: downloadTarget} : {};
  }

  /**
   * @param {string | null} currentSetting
   * @param {string} downloadTarget
   * @returns {Promise<string | null>}
   */
  async function pickJavaBinary(currentSetting, downloadTarget) {
    const parentWindow = getParentWindow();
    if (parentWindow == null) {
      return null;
    }

    /** @type {import('electron').OpenDialogOptions} */
    const dialogOptions = {
      title: 'Select Java runtime',
      properties: ['openFile', 'openDirectory'],
      ...defaultPathOf(currentSetting, downloadTarget)
    };

    const {canceled, filePaths} = await dialog.showOpenDialog(parentWindow, dialogOptions);
    const filePath = filePaths[0];
    return canceled || filePath == null ? null : filePath;
  }

  return {pickJavaBinary};
}

module.exports = {createJavaDialogs};
