import type {Config} from 'jest';

// Aggregator over the two suites, which share nothing: `npx jest <path-to-spec>` and IDE jest integrations
// resolve this default config and pick the matching project themselves.
const config: Config = {
  projects: [
    '<rootDir>/jest.angular.config.ts',
    '<rootDir>/jest.electron.config.ts'
  ]
};

export default config;
