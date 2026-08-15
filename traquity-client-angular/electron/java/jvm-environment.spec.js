const {beforeEach, describe, expect, it} = require('@jest/globals');
const {jvmEnvironment} = require('./jvm-environment.js');

describe('jvmEnvironment', () => {
  /** @type {NodeJS.ProcessEnv} */
  let environment;

  beforeEach(() => {
    environment = {PATH: '/usr/bin', HOME: '/home/x'};
  });

  it('keeps every entry a JVM needs', () => {
    expect(jvmEnvironment(environment)).toEqual({PATH: '/usr/bin', HOME: '/home/x'});
  });

  it('strips the variables a JVM takes extra command-line arguments from', () => {
    environment['JAVA_TOOL_OPTIONS'] = '-javaagent:/tmp/tool.jar';
    environment['JDK_JAVA_OPTIONS'] = '-javaagent:/tmp/jdk.jar';
    environment['_JAVA_OPTIONS'] = '-javaagent:/tmp/underscore.jar';

    expect(jvmEnvironment(environment)).toEqual({PATH: '/usr/bin', HOME: '/home/x'});
  });

  it('strips the variables the dynamic linker loads shared objects from', () => {
    environment['LD_PRELOAD'] = '/tmp/preload.so';
    environment['LD_AUDIT'] = '/tmp/audit.so';
    environment['DYLD_INSERT_LIBRARIES'] = '/tmp/insert.dylib';

    expect(jvmEnvironment(environment)).toEqual({PATH: '/usr/bin', HOME: '/home/x'});
  });

  it('strips the database password', () => {
    environment['TQ_DB_FILE_PASSWORD'] = 'hunter2';

    expect(jvmEnvironment(environment)).toEqual({PATH: '/usr/bin', HOME: '/home/x'});
  });

  it('leaves the environment it was given untouched', () => {
    environment['_JAVA_OPTIONS'] = '-javaagent:/tmp/underscore.jar';

    jvmEnvironment(environment);

    expect(environment).toEqual({PATH: '/usr/bin', HOME: '/home/x', _JAVA_OPTIONS: '-javaagent:/tmp/underscore.jar'});
  });
});
