import {beforeEach, describe, expect, it} from '@jest/globals';
import {FileDirectoryPipe} from './file-directory.pipe';

describe('FileDirectoryPipe', (): void => {
  let pipe: FileDirectoryPipe;

  beforeEach((): void => {
    pipe = new FileDirectoryPipe();
  });

  it('takes everything before the last segment of a Windows path', (): void => {
    expect(pipe.transform('C:\\Users\\x\\fynancials.config.json')).toBe('C:\\Users\\x');
  });

  it('takes everything before the last segment of a POSIX path', (): void => {
    expect(pipe.transform('/home/x/fynancials.config.json')).toBe('/home/x');
  });

  it('returns an empty string for a bare name with no separator', (): void => {
    expect(pipe.transform('fynancials')).toBe('');
  });

  it('keeps the root as the directory of a name directly under it', (): void => {
    expect(pipe.transform('/fynancials.config.json')).toBe('/');
  });

  it('keeps the drive root as the directory of a name directly under it', (): void => {
    expect(pipe.transform('C:\\fynancials.config.json')).toBe('C:\\');
  });

  it('ignores a trailing separator', (): void => {
    expect(pipe.transform('/home/x/fynancials/')).toBe('/home/x');
  });

  it('returns an empty string for a null path', (): void => {
    expect(pipe.transform(null)).toBe('');
  });
});
