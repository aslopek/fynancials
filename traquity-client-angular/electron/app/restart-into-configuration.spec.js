const {beforeEach, describe, expect, it, jest} = require('@jest/globals');
const {createRestartIntoConfiguration} = require('./restart-into-configuration.js');

/** @import {RestartIntoConfiguration} from './restart-into-configuration.js' */
/** @import {BackendProcess} from '../backend/backend-process.js' */
/** @import {ConfigureOnNextStart} from '../config/configure-on-next-start.js' */

describe('restartIntoConfiguration', () => {
  /** @type {string[]} */
  let calls;

  /** @type {Pick<ConfigureOnNextStart, 'request'>} */
  let configureOnNextStart;

  /** @type {Pick<BackendProcess, 'kill'>} */
  let backendProcess;

  /** @type {Pick<import('electron').App, 'relaunch' | 'exit'>} */
  let app;

  /** @type {RestartIntoConfiguration} */
  let restartIntoConfiguration;

  const request = jest.fn(() => {
    calls.push('request()');
  });
  const kill = jest.fn(() => {
    calls.push('kill()');
  });
  const relaunch = jest.fn(() => {
    calls.push('relaunch()');
  });
  const exit = jest.fn(/** @type {(code?: number) => void} */ ((code) => {
    calls.push(`exit(${code})`);
  }));

  beforeEach(() => {
    jest.clearAllMocks();
    calls = [];

    configureOnNextStart = {request};
    backendProcess = {kill};
    app = {relaunch, exit};

    restartIntoConfiguration = createRestartIntoConfiguration({configureOnNextStart, backendProcess, app});
  });

  it('sets the flag, kills the backend, relaunches and exits, in that order', () => {
    restartIntoConfiguration.restart();

    expect(calls).toEqual(['request()', 'kill()', 'relaunch()', 'exit(0)']);
  });

  it('calls request with no arguments', () => {
    restartIntoConfiguration.restart();

    expect(request).toHaveBeenCalledTimes(1);
    expect(request).toHaveBeenCalledWith();
  });

  it('calls kill with no arguments', () => {
    restartIntoConfiguration.restart();

    expect(kill).toHaveBeenCalledTimes(1);
    expect(kill).toHaveBeenCalledWith();
  });

  it('calls relaunch with no arguments', () => {
    restartIntoConfiguration.restart();

    expect(relaunch).toHaveBeenCalledTimes(1);
    expect(relaunch).toHaveBeenCalledWith();
  });

  it('exits with code 0', () => {
    restartIntoConfiguration.restart();

    expect(exit).toHaveBeenCalledTimes(1);
    expect(exit).toHaveBeenCalledWith(0);
  });
});
