/**
 * The two native file dialogs for choosing a database: pick an existing one, or name a new one. Electron's `dialog`
 * and the main window are injected, so this module is exercisable without an Electron instance - which is why the
 * dialogs live here and not in `main-window.js`.
 *
 * Everything crossing this boundary is a **base path without extension**. The `.mv.db` suffix H2 appends exists only
 * inside the dialogs, and is stripped here.
 *
 * No database file is ever created, truncated or deleted: there is no writer among the injected file-system members.
 * A file the user chooses in the save dialog is only *reported* as existing; what follows from that is decided
 * elsewhere.
 */

/** The file H2 materializes for a base path; the one the user actually sees and picks. */
const DATABASE_FILE_SUFFIX = '.mv.db';

const DATABASE_FILE_FILTER = {
  name: 'h2 database',
  extensions: ['mv.db']
};

/**
 * Electron's `dialog`, narrowed to the two calls made here.
 *
 * The parent window is a type parameter rather than `BrowserWindow`: this module only ever hands the window it was
 * given straight back to the dialog it was given, and never touches a member of it. Naming the real class here would
 * force a spec to construct one - which no stub can - for a value the module treats as opaque.
 *
 * @template TWindow
 * @typedef {Object} DialogLike
 * @property {(window: TWindow, options: import('electron').OpenDialogOptions) =>
 *   Promise<import('electron').OpenDialogReturnValue>} showOpenDialog
 * @property {(window: TWindow, options: import('electron').SaveDialogOptions) =>
 *   Promise<import('electron').SaveDialogReturnValue>} showSaveDialog
 */

/**
 * The one function needed from `fs`.
 *
 * @typedef {Object} DatabaseFileSystem
 * @property {(path: string) => boolean} existsSync
 */

/**
 * What a save dialog produced, and whether the file it names is already there: the disk fact travels, no verdict
 * about it does.
 *
 * @typedef {Object} PickedDatabase
 * @property {string} basePath database base path without extension
 * @property {boolean} fileExists whether `<basePath>.mv.db` is on disk
 */

/**
 * @typedef {Object} DatabaseDialogs
 * @property {(currentSelection: string | null) => Promise<string | null>} pickExisting
 * @property {(currentSelection: string | null) => Promise<PickedDatabase | null>} pickNew
 */

/**
 * @template TWindow
 * @typedef {Object} DatabaseDialogsOptions
 * @property {DialogLike<TWindow>} dialog
 * @property {() => TWindow | null} getParentWindow the window a dialog is parented to, so it cannot end up behind
 *   the app. Without one there is nothing to parent to and nothing to return a selection to, so no dialog opens.
 * @property {DatabaseFileSystem} fileSystem
 */

/**
 * @template TWindow
 * @param {DatabaseDialogsOptions<TWindow>} options
 * @returns {DatabaseDialogs}
 */
function createDatabaseDialogs(options) {
  const {dialog, getParentWindow, fileSystem} = options;

  /**
   * Strips the H2 file suffix, case-insensitively: a user typing `MyDb.MV.DB` into the save dialog must not end up
   * with that as their base path.
   *
   * @param {string} filePath
   * @returns {string}
   */
  function baseNameOf(filePath) {
    const suffixStart = filePath.length - DATABASE_FILE_SUFFIX.length;
    const carriesSuffix = suffixStart > 0 && filePath.slice(suffixStart).toLowerCase() === DATABASE_FILE_SUFFIX;
    return carriesSuffix ? filePath.slice(0, suffixStart) : filePath;
  }

  /**
   * @param {string | null} currentSelection
   * @returns {{defaultPath?: string}}
   */
  function defaultPathOf(currentSelection) {
    return currentSelection == null ? {} : {defaultPath: `${currentSelection}${DATABASE_FILE_SUFFIX}`};
  }

  /**
   * @param {string | null} currentSelection
   * @returns {Promise<string | null>}
   */
  async function pickExisting(currentSelection) {
    const parentWindow = getParentWindow();
    if (parentWindow == null) {
      return null;
    }

    /** @type {import('electron').OpenDialogOptions} */
    const dialogOptions = {
      title: 'Use existing database',
      filters: [DATABASE_FILE_FILTER],
      properties: ['openFile'],
      ...defaultPathOf(currentSelection)
    };

    const {canceled, filePaths} = await dialog.showOpenDialog(parentWindow, dialogOptions);
    const filePath = filePaths[0];
    // no existence check: an open dialog only ever offers files that are there
    return canceled || filePath == null ? null : baseNameOf(filePath);
  }

  /**
   * `showOverwriteConfirmation` is deliberately not set: it is Linux-only, while Windows and macOS confirm on their
   * own regardless and cannot be talked out of it.
   *
   * @param {string | null} currentSelection
   * @returns {Promise<PickedDatabase | null>}
   */
  async function pickNew(currentSelection) {
    const parentWindow = getParentWindow();
    if (parentWindow == null) {
      return null;
    }

    /** @type {import('electron').SaveDialogOptions} */
    const dialogOptions = {
      title: 'Create new database',
      filters: [DATABASE_FILE_FILTER],
      ...defaultPathOf(currentSelection)
    };

    const {canceled, filePath} = await dialog.showSaveDialog(parentWindow, dialogOptions);
    if (canceled || filePath === '') {
      return null;
    }

    const basePath = baseNameOf(filePath);
    return {basePath, fileExists: fileSystem.existsSync(`${basePath}${DATABASE_FILE_SUFFIX}`)};
  }

  return {pickExisting, pickNew};
}

module.exports = {createDatabaseDialogs};
