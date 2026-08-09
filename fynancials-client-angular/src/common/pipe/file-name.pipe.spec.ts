import {beforeEach, describe, expect, it} from '@jest/globals';
import {FileNamePipe} from './file-name.pipe';

describe('FileNamePipe', (): void => {
  let pipe: FileNamePipe;

  beforeEach((): void => {
    pipe = new FileNamePipe();
  });

  it('takes the last segment of a Windows path', (): void => {
    expect(pipe.transform('C:\\Users\\x\\fynancials')).toBe('fynancials');
  });

  it('takes the last segment of a POSIX path', (): void => {
    expect(pipe.transform('/home/x/fynancials')).toBe('fynancials');
  });

  it('returns a bare name with no separator unchanged', (): void => {
    expect(pipe.transform('fynancials')).toBe('fynancials');
  });

  it('ignores a trailing separator', (): void => {
    expect(pipe.transform('C:\\Users\\x\\fynancials\\')).toBe('fynancials');
  });

  it('returns an empty string for a null path', (): void => {
    expect(pipe.transform(null)).toBe('');
  });

  it('returns an empty string for an empty path', (): void => {
    expect(pipe.transform('')).toBe('');
  });

  it('appends the given extension to the name', (): void => {
    expect(pipe.transform('C:\\Users\\x\\fynancials', '.mv.db')).toBe('fynancials.mv.db');
  });

  it('renders no extension for no name, even when one is given', (): void => {
    expect(pipe.transform(null, '.mv.db')).toBe('');
  });

  it('does not append the extension a second time when the name already ends with it', (): void => {
    expect(pipe.transform('C:\\Users\\x\\fynancials.mv.db', '.mv.db')).toBe('fynancials.mv.db');
  });
});
