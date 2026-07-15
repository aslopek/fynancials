import type {Config} from 'jest';

const config: Config = {
  preset: 'ts-jest',
  testEnvironment: 'node',
  testMatch: ['<rootDir>/src/**/*.spec.ts'],
  setupFiles: ['<rootDir>/jest.setup.ts'],
  transform: {
    '^.+\\.ts$': ['ts-jest', {
      // merged onto tsconfig.json by ts-jest; only `module` needs overriding for jest's CJS loader
      tsconfig: {module: 'commonjs'}
    }],
    // @ngrx packages ship ESM-only (Angular Package Format); transpile their import/export
    // syntax to CommonJS so jest's CJS module loader can require() them like everything else.
    '^.+\\.m?js$': ['babel-jest', {
      presets: [
        [
          '@babel/preset-env',
          {
            targets: {node: 'current'}
          }
        ]
      ]
    }]
  },
  transformIgnorePatterns: ['node_modules/(?!(@ngrx|@angular)/)']
};

export default config;
