const {beforeEach, describe, expect, it, jest} = require('@jest/globals');
const {createDatabaseDialogs} = require('./database-dialogs.js');

/** @import {DatabaseDialogs, DatabaseFileSystem, DialogLike} from './database-dialogs.js' */

/**
 * Stands in for the `BrowserWindow` the dialogs are parented to. The module hands it straight back to the injected
 * dialog and never reads a member of it, which is exactly why its type is a parameter.
 *
 * @typedef {{label: string}} TestWindow
 */

describe('databaseDialogs', () => {
  const currentSelection = 'C:\\dbs\\fynancials';
  const pickedFilePath = 'C:\\dbs\\other.mv.db';
  const savedFilePath = 'C:\\dbs\\new';

  /** @type {TestWindow} */
  let parentWindow;

  /** @type {TestWindow | null} */
  let window;

  /** @type {DatabaseDialogs} */
  let dialogs;

  const showOpenDialog = jest.fn(/** @type {DialogLike<TestWindow>['showOpenDialog']} */
    (() => Promise.resolve({canceled: false, filePaths: [pickedFilePath]})));
  const showSaveDialog = jest.fn(/** @type {DialogLike<TestWindow>['showSaveDialog']} */
    (() => Promise.resolve({canceled: false, filePath: savedFilePath})));
  const existsSync = jest.fn(/** @type {DatabaseFileSystem['existsSync']} */ (() => false));

  beforeEach(() => {
    parentWindow = {label: 'main'};
    jest.clearAllMocks();
    showOpenDialog.mockResolvedValue({canceled: false, filePaths: [pickedFilePath]});
    showSaveDialog.mockResolvedValue({canceled: false, filePath: savedFilePath});
    existsSync.mockReturnValue(false);
    window = parentWindow;

    /** @type {DialogLike<TestWindow>} */
    const dialog = {showOpenDialog, showSaveDialog};

    /** @type {DatabaseFileSystem} */
    const fileSystem = {existsSync};

    dialogs = createDatabaseDialogs({
      dialog,
      getParentWindow: () => window,
      fileSystem
    });
  });

  describe('pickExisting', () => {
    it('returns the picked file as a base path', async () => {
      await expect(dialogs.pickExisting(currentSelection)).resolves.toBe('C:\\dbs\\other');
    });

    it('filters the open dialog to the H2 file and parents it to the main window', async () => {
      await dialogs.pickExisting(currentSelection);

      expect(showOpenDialog).toHaveBeenCalledTimes(1);
      expect(showOpenDialog).toHaveBeenCalledWith(parentWindow, {
        title: 'Use existing database',
        filters: [
          {
            name: 'h2 database',
            extensions: ['mv.db']
          }
        ],
        properties: ['openFile'],
        defaultPath: `${currentSelection}.mv.db`
      });
    });

    it('opens without a default path when nothing is selected yet', async () => {
      await dialogs.pickExisting(null);

      expect(showOpenDialog).toHaveBeenCalledTimes(1);
      expect(showOpenDialog).toHaveBeenCalledWith(parentWindow, {
        title: 'Use existing database',
        filters: [
          {
            name: 'h2 database',
            extensions: ['mv.db']
          }
        ],
        properties: ['openFile']
      });
    });

    it('checks no file for a database the open dialog offered', async () => {
      await dialogs.pickExisting(currentSelection);

      expect(existsSync).not.toHaveBeenCalled();
    });

    it('returns null when the dialog is cancelled', async () => {
      showOpenDialog.mockResolvedValue({canceled: true, filePaths: []});

      await expect(dialogs.pickExisting(currentSelection)).resolves.toBeNull();
    });

    it('returns null when the dialog yields no path', async () => {
      showOpenDialog.mockResolvedValue({canceled: false, filePaths: []});

      await expect(dialogs.pickExisting(currentSelection)).resolves.toBeNull();
    });

    it('opens no dialog without a main window', async () => {
      window = null;

      await expect(dialogs.pickExisting(currentSelection)).resolves.toBeNull();
      expect(showOpenDialog).not.toHaveBeenCalled();
    });
  });

  describe('pickNew', () => {
    it('reports a named file that is not on disk as non-existing', async () => {
      await expect(dialogs.pickNew(currentSelection)).resolves.toEqual({
        basePath: savedFilePath,
        fileExists: false
      });
    });

    it('filters the save dialog to the H2 file and parents it to the main window', async () => {
      await dialogs.pickNew(currentSelection);

      expect(showSaveDialog).toHaveBeenCalledTimes(1);
      expect(showSaveDialog).toHaveBeenCalledWith(parentWindow, {
        title: 'Create new database',
        filters: [
          {
            name: 'h2 database',
            extensions: ['mv.db']
          }
        ],
        defaultPath: `${currentSelection}.mv.db`
      });
    });

    it('checks the H2 file rather than the base path', async () => {
      await dialogs.pickNew(currentSelection);

      expect(existsSync).toHaveBeenCalledTimes(1);
      expect(existsSync).toHaveBeenCalledWith(`${savedFilePath}.mv.db`);
    });

    it('reports a named file that is on disk as existing', async () => {
      existsSync.mockReturnValue(true);

      await expect(dialogs.pickNew(currentSelection)).resolves.toEqual({
        basePath: savedFilePath,
        fileExists: true
      });
    });

    it('strips a suffix the platform appended', async () => {
      showSaveDialog.mockResolvedValue({canceled: false, filePath: `${savedFilePath}.mv.db`});

      await expect(dialogs.pickNew(currentSelection)).resolves.toEqual({
        basePath: savedFilePath,
        fileExists: false
      });
    });

    it('strips the suffix case-insensitively', async () => {
      showSaveDialog.mockResolvedValue({canceled: false, filePath: 'C:\\dbs\\New.MV.DB'});

      await expect(dialogs.pickNew(currentSelection)).resolves.toEqual({
        basePath: 'C:\\dbs\\New',
        fileExists: false
      });
    });

    it('returns null when the dialog is cancelled', async () => {
      showSaveDialog.mockResolvedValue({canceled: true, filePath: ''});

      await expect(dialogs.pickNew(currentSelection)).resolves.toBeNull();
      expect(existsSync).not.toHaveBeenCalled();
    });

    it('returns null when the dialog yields no path', async () => {
      showSaveDialog.mockResolvedValue({canceled: false, filePath: ''});

      await expect(dialogs.pickNew(currentSelection)).resolves.toBeNull();
      expect(existsSync).not.toHaveBeenCalled();
    });

    it('opens no dialog without a main window', async () => {
      window = null;

      await expect(dialogs.pickNew(currentSelection)).resolves.toBeNull();
      expect(showSaveDialog).not.toHaveBeenCalled();
    });
  });
});
