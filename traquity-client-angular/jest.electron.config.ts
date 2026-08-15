import type {Config} from 'jest';

const config: Config = {
  displayName: 'electron',
  testEnvironment: 'node',
  testMatch: ['<rootDir>/electron/**/*.spec.js'],
  // the specs import every jest symbol from '@jest/globals' explicitly; without this, jest's injected `jest`
  // wrapper argument collides with that import in a CommonJS file
  injectGlobals: false,
  // the Electron main process loads these files as plain CommonJS; running them untransformed is what keeps
  // the specs honest about what actually ships
  transform: {}
};

export default config;
