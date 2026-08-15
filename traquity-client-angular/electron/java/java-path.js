const path = require('node:path');

/**
 * Resolves `java` to an absolute path before it is ever spawned, rather than leaving the OS to resolve the bare
 * command name at spawn time. Every answer is therefore one named binary.
 *
 * The target platform is a parameter rather than a read of `process.platform`, so a spec can exercise either branch
 * on any host OS.
 */

/**
 * What `cmd.exe` falls back to when `PATHEXT` is unset, so a Windows lookup still has candidate extensions.
 */
const DEFAULT_PATHEXT = '.EXE;.BAT;.CMD;.COM';

/**
 * @typedef {Object} JavaFileSystem
 * @property {(path: string) => boolean} existsSync
 * @property {(path: string) => {isDirectory: () => boolean}} statSync
 */

/**
 * @param {Pick<NodeJS.ProcessEnv, 'PATHEXT'>} environment
 * @returns {boolean}
 */
function isWindowsEnvironment(environment) {
  return environment.PATHEXT != null;
}

/**
 * @param {string} pathext
 * @param {import('node:path').PlatformPath} platformPath
 * @returns {string[]}
 */
function windowsCandidateNames(pathext, platformPath) {
  return pathext
    .split(platformPath.delimiter)
    .filter(extension => extension.length > 0)
    .map(extension => `java${extension.toLowerCase()}`);
}

/**
 * @param {Pick<NodeJS.ProcessEnv, 'PATH' | 'PATHEXT'>} environment
 * @param {JavaFileSystem} fileSystem
 * @param {NodeJS.Platform} platform
 * @returns {string | null}
 */
function findJavaOnPath(environment, fileSystem, platform) {
  const isWindows = platform === 'win32';
  const platformPath = isWindows ? path.win32 : path.posix;
  const directories = (environment.PATH ?? '').split(platformPath.delimiter).filter(directory => platformPath.isAbsolute(directory));
  const candidateNames = isWindows ? windowsCandidateNames(environment.PATHEXT ?? DEFAULT_PATHEXT, platformPath) : ['java'];

  for (const directory of directories) {
    for (const candidateName of candidateNames) {
      const candidate = platformPath.join(directory, candidateName);
      if (fileSystem.existsSync(candidate)) {
        return candidate;
      }
    }
  }
  return null;
}

/**
 * @param {string} candidatePath
 * @param {JavaFileSystem} fileSystem
 * @returns {boolean}
 */
function isDirectory(candidatePath, fileSystem) {
  try {
    return fileSystem.statSync(candidatePath).isDirectory();
  } catch {
    return false;
  }
}

/**
 * @param {string} candidatePath
 * @param {JavaFileSystem} fileSystem
 * @param {NodeJS.Platform} platform
 * @returns {string}
 */
function normalizeToJavaBinary(candidatePath, fileSystem, platform) {
  const isWindows = platform === 'win32';
  const platformPath = isWindows ? path.win32 : path.posix;
  const trimmed = candidatePath.replace(/[/\\]+$/, '');

  if (!isDirectory(trimmed, fileSystem)) {
    return trimmed;
  }
  return platformPath.join(trimmed, 'bin', isWindows ? 'java.exe' : 'java');
}

module.exports = {findJavaOnPath, normalizeToJavaBinary};
