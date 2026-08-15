const {beforeEach, describe, expect, it} = require('@jest/globals');
const {isTrustedSender} = require('./trusted-sender.js');

/** @import {MainFrameHolder} from './trusted-sender.js' */

describe('isTrustedSender', () => {
  /** @type {object} the object identity the decision is made against, opaque to the module under test */
  let mainFrame;

  /** @type {MainFrameHolder} */
  let window;

  /** @type {{senderFrame: unknown}} */
  let event;

  beforeEach(() => {
    mainFrame = {};
    window = {webContents: {mainFrame}};
    event = {senderFrame: mainFrame};
  });

  it('trusts an event sent by the window\'s own main frame', () => {
    expect(isTrustedSender(event, window)).toBe(true);
  });

  it('refuses an event sent by another frame of the same window', () => {
    event = {senderFrame: {}};

    expect(isTrustedSender(event, window)).toBe(false);
  });

  it('refuses an event whose sending frame is already gone', () => {
    event = {senderFrame: null};

    expect(isTrustedSender(event, window)).toBe(false);
  });

  it('refuses an event carrying no sender frame at all', () => {
    expect(isTrustedSender({}, window)).toBe(false);
  });

  it('refuses a non-object event', () => {
    expect(isTrustedSender('startup:getState', window)).toBe(false);
  });

  it('refuses an undefined event', () => {
    expect(isTrustedSender(undefined, window)).toBe(false);
  });

  it('refuses every event while there is no window to compare against', () => {
    expect(isTrustedSender(event, null)).toBe(false);
  });
});
