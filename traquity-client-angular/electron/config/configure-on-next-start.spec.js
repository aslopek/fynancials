const {beforeEach, describe, expect, it, jest} = require('@jest/globals');
const {createConfigureOnNextStart} = require('./configure-on-next-start.js');

/** @import {ConfigureOnNextStart} from './configure-on-next-start.js' */
/** @import {ConfigFile} from './config-file.js' */
/** @import {TraQuityConfig} from './config-schema.js' */

describe('configureOnNextStart', () => {
  const databasePath = 'C:\\Users\\x\\traquity';

  /** @type {TraQuityConfig} */
  let config;

  /** @type {Pick<ConfigFile, 'save'>} */
  let configFile;

  /** @type {ConfigureOnNextStart} */
  let configureOnNextStart;

  const save = jest.fn();

  beforeEach(() => {
    jest.clearAllMocks();

    config = {
      env: {TQ_DB_FILE_PATH: databasePath},
      auth: {[databasePath]: {passwordless: true}},
      java: {path: null, signature: null}
    };

    configFile = {save};

    configureOnNextStart = createConfigureOnNextStart({configFile, config});
  });

  it('sets the flag and saves the whole loaded config unchanged otherwise', () => {
    configureOnNextStart.request();

    expect(save).toHaveBeenCalledTimes(1);
    expect(save).toHaveBeenCalledWith({
      env: {TQ_DB_FILE_PATH: databasePath},
      auth: {[databasePath]: {passwordless: true}},
      java: {path: null, signature: null},
      configureOnNextStart: true
    });
  });

  describe('with the flag already present and false', () => {
    beforeEach(() => {
      config.configureOnNextStart = false;
    });

    it('flips it to true', () => {
      configureOnNextStart.request();

      expect(config.configureOnNextStart).toBe(true);
    });
  });
});
