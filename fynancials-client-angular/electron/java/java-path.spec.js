const {beforeEach, describe, expect, it, jest} = require('@jest/globals');
const {findJavaOnPath, normalizeToJavaBinary} = require('./java-path.js');

/** @import {JavaFileSystem} from './java-path.js' */

describe('javaPath', () => {
  const existsSync = jest.fn(/** @type {JavaFileSystem['existsSync']} */ (() => false));
  const statSync = jest.fn(/** @type {JavaFileSystem['statSync']} */ (() => ({isDirectory: () => false})));

  /** @type {JavaFileSystem} */
  let fileSystem;

  beforeEach(() => {
    jest.clearAllMocks();
    existsSync.mockReturnValue(false);
    statSync.mockReturnValue({isDirectory: () => false});
    fileSystem = {existsSync, statSync};
  });

  describe('findJavaOnPath', () => {
    /** @type {Pick<NodeJS.ProcessEnv, 'PATH' | 'PATHEXT'>} */
    let environment;

    /** @type {NodeJS.Platform} */
    let platform;

    beforeEach(() => {
      jest.resetAllMocks();
      environment = {PATH: '/opt/jdk/bin:/usr/local/bin', PATHEXT: undefined};
      platform = 'linux';
    });

    it('returns the first existing java candidate on a posix-style PATH', () => {
      existsSync.mockImplementation((candidate) => candidate === '/usr/local/bin/java');

      expect(findJavaOnPath(environment, fileSystem, platform)).toBe('/usr/local/bin/java');
    });

    it('checks each directory in order for the plain candidate name', () => {
      findJavaOnPath(environment, fileSystem, platform);

      expect(existsSync.mock.calls).toEqual([
        ['/opt/jdk/bin/java'],
        ['/usr/local/bin/java']
      ]);
    });

    it('ignores PATHEXT on a non-Windows platform', () => {
      environment = {PATH: '/opt/jdk/bin:/usr/local/bin', PATHEXT: '.COM;.EXE;.BAT'};

      findJavaOnPath(environment, fileSystem, platform);

      expect(existsSync.mock.calls).toEqual([
        ['/opt/jdk/bin/java'],
        ['/usr/local/bin/java']
      ]);
    });

    it('returns null when no candidate exists', () => {
      expect(findJavaOnPath(environment, fileSystem, platform)).toBeNull();
    });

    it('returns null for an absent PATH', () => {
      environment = {PATH: undefined, PATHEXT: undefined};

      expect(findJavaOnPath(environment, fileSystem, platform)).toBeNull();
      expect(existsSync).not.toHaveBeenCalled();
    });

    it('returns null for an empty PATH', () => {
      environment = {PATH: '', PATHEXT: undefined};

      expect(findJavaOnPath(environment, fileSystem, platform)).toBeNull();
      expect(existsSync).not.toHaveBeenCalled();
    });

    it('passes over a relative PATH entry and keeps looking in the absolute ones', () => {
      environment = {PATH: '.:relative/bin:/usr/local/bin', PATHEXT: undefined};
      existsSync.mockImplementation((candidate) => candidate === '/usr/local/bin/java');

      expect(findJavaOnPath(environment, fileSystem, platform)).toBe('/usr/local/bin/java');
      expect(existsSync.mock.calls).toEqual([['/usr/local/bin/java']]);
    });

    describe('on win32', () => {
      beforeEach(() => {
        environment = {PATH: 'C:\\jdk\\bin;C:\\other\\bin', PATHEXT: '.COM;.EXE;.BAT'};
        platform = 'win32';
      });

      it('tries java plus each PATHEXT extension, lowercased, per directory and never the bare name', () => {
        findJavaOnPath(environment, fileSystem, platform);

        expect(existsSync.mock.calls).toEqual([
          ['C:\\jdk\\bin\\java.com'],
          ['C:\\jdk\\bin\\java.exe'],
          ['C:\\jdk\\bin\\java.bat'],
          ['C:\\other\\bin\\java.com'],
          ['C:\\other\\bin\\java.exe'],
          ['C:\\other\\bin\\java.bat']
        ]);
      });

      it('returns the first existing extensioned candidate', () => {
        existsSync.mockImplementation((candidate) => candidate === 'C:\\jdk\\bin\\java.exe');

        expect(findJavaOnPath(environment, fileSystem, platform)).toBe('C:\\jdk\\bin\\java.exe');
      });

      it('falls back to the cmd.exe default extensions for an absent PATHEXT', () => {
        environment = {PATH: 'C:\\jdk\\bin;C:\\other\\bin', PATHEXT: undefined};

        findJavaOnPath(environment, fileSystem, platform);

        expect(existsSync.mock.calls).toEqual([
          ['C:\\jdk\\bin\\java.exe'],
          ['C:\\jdk\\bin\\java.bat'],
          ['C:\\jdk\\bin\\java.cmd'],
          ['C:\\jdk\\bin\\java.com'],
          ['C:\\other\\bin\\java.exe'],
          ['C:\\other\\bin\\java.bat'],
          ['C:\\other\\bin\\java.cmd'],
          ['C:\\other\\bin\\java.com'],
        ]);
      });
    });
  });

  describe('normalizeToJavaBinary', () => {
    it('returns a file path unchanged', () => {
      expect(normalizeToJavaBinary('/opt/jdk/bin/java', fileSystem, 'linux')).toBe('/opt/jdk/bin/java');
    });

    it('appends bin/java to a posix directory', () => {
      statSync.mockReturnValue({isDirectory: () => true});

      expect(normalizeToJavaBinary('/opt/jdk', fileSystem, 'linux')).toBe('/opt/jdk/bin/java');
    });

    it('appends bin\\java.exe to a windows directory', () => {
      statSync.mockReturnValue({isDirectory: () => true});

      expect(normalizeToJavaBinary('C:\\jdk', fileSystem, 'win32')).toBe('C:\\jdk\\bin\\java.exe');
    });

    it('strips a trailing separator before deciding directory-ness', () => {
      statSync.mockReturnValue({isDirectory: () => true});

      normalizeToJavaBinary('/opt/jdk/', fileSystem, 'linux');

      expect(statSync).toHaveBeenCalledTimes(1);
      expect(statSync).toHaveBeenCalledWith('/opt/jdk');
    });

    it('strips a trailing separator from a file path', () => {
      expect(normalizeToJavaBinary('/opt/jdk/bin/java/', fileSystem, 'linux')).toBe('/opt/jdk/bin/java');
    });

    it('treats a path that does not exist as a file, deferring the verdict to -version', () => {
      statSync.mockImplementation(() => {
        throw new Error('ENOENT');
      });

      expect(normalizeToJavaBinary('/does/not/exist', fileSystem, 'linux')).toBe('/does/not/exist');
    });
  });
});
