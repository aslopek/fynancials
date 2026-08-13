const {describe, expect, it} = require('@jest/globals');
const {isOpenableExternally, isSameDocument} = require('./navigation-policy.js');

describe('isOpenableExternally', () => {
  it('opens an https URL', () => {
    expect(isOpenableExternally('https://github.com/')).toBe(true);
  });

  it('opens a plain http URL', () => {
    expect(isOpenableExternally('http://localhost:23726/config/pid')).toBe(true);
  });

  it('refuses a file URL', () => {
    expect(isOpenableExternally('file:///C:/Windows/System32/calc.exe')).toBe(false);
  });

  it('refuses a javascript URL', () => {
    expect(isOpenableExternally('javascript:fetch("https://example.invalid")')).toBe(false);
  });

  it('refuses an OS-specific handler scheme', () => {
    expect(isOpenableExternally('ms-msdt:/id PCWDiagnostic')).toBe(false);
  });

  it('refuses a UNC path', () => {
    expect(isOpenableExternally('\\\\attacker.invalid\\share\\payload.exe')).toBe(false);
  });

  it('refuses a string that is no URL at all', () => {
    expect(isOpenableExternally('not a url')).toBe(false);
  });

  it('refuses an empty string', () => {
    expect(isOpenableExternally('')).toBe(false);
  });
});

describe('isSameDocument', () => {
  const documentUrl = 'file:///C:/app/index.html';

  it('admits the document itself', () => {
    expect(isSameDocument(documentUrl, documentUrl)).toBe(true);
  });

  it('admits the document with a fragment the router appended', () => {
    expect(isSameDocument(`${documentUrl}#/depot/1`, documentUrl)).toBe(true);
  });

  it('admits the document with a query string', () => {
    expect(isSameDocument(`${documentUrl}?reload=1`, documentUrl)).toBe(true);
  });

  it('refuses a remote origin', () => {
    expect(isSameDocument('https://attacker.invalid/', documentUrl)).toBe(false);
  });

  it('refuses another file in the same directory', () => {
    expect(isSameDocument('file:///C:/app/other.html', documentUrl)).toBe(false);
  });

  it('refuses a path that only starts with the document path', () => {
    expect(isSameDocument(`${documentUrl}.evil`, documentUrl)).toBe(false);
  });

  it('refuses a string that is no URL at all', () => {
    expect(isSameDocument('index.html', documentUrl)).toBe(false);
  });
});
