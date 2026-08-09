const path = require('path');
const fs = require('fs');
const {rimrafSync} = require('rimraf')

// What ends up inside app.asar. electron-packager copies the whole package directory unless told otherwise, which
// would ship the Angular sources, every spec, the build tooling and a second copy of backend.jar (it is already
// delivered as an extraResource). Hence an allowlist rather than a list of exclusions: a file added later ships only
// if it lands somewhere named here, instead of shipping by default and being noticed by nobody.
const packagedPaths = [
    '/package.json',            // Electron resolves the "main" entry point through it
    '/electron',                // the main process
    '/dist/fynancials/browser', // the built Angular app that electron/main.js loads
    '/node_modules'             // production dependency closure - electron-packager prunes the devDependencies itself
];

// Carve-outs within the paths above. The spec-only files are the point of the exercise: electron/testing/base64-of.js
// requires `expect`, a devDependency that gets pruned away, so shipping it would put test code that cannot even load
// into the product - and suggest a test library belongs to it.
const unpackagedPaths = [
    /\.spec\.js$/,
    /^\/electron\/testing($|\/)/,
    /^\/electron\/.*\.md$/,
    // electron-packager drops these itself only while `ignore` is a list of patterns; a function replaces its
    // defaults instead of extending them, so what is still relevant of them has to be restated here
    /^\/node_modules\/\.bin($|\/)/
];

/**
 * Paths arrive relative to this directory, POSIX-separated and with a leading slash; the directory itself arrives as
 * an empty string. A directory has to survive for anything below it to be visited at all - that is the third clause.
 *
 * @param {string} filePath
 * @returns {boolean}
 */
function isPackaged(filePath) {
    if (unpackagedPaths.some(unpackagedPath => unpackagedPath.test(filePath))) {
        return false;
    }
    return packagedPaths.some(packagedPath => filePath === packagedPath
      || filePath.startsWith(`${packagedPath}/`)
      || packagedPath.startsWith(`${filePath}/`));
}

module.exports = {
    hooks: {
        generateAssets: async (_config, _buildPath, _electronVersion, _platform, _arch) => {
            const fileName = `fynancials-server-spring-${require('./package.json').version}.jar`;
            const src = path.join(__dirname, '..', 'fynancials-server-spring', 'target', fileName);
            const resources = path.join(__dirname, 'resources');
            const dst = path.join(resources, 'backend.jar');

            rimrafSync(resources);
            fs.mkdirSync(resources);
            fs.cpSync(src, dst);
        }
    },
    packagerConfig: {
        asar: true,
        icon: 'src/assets/icon',
        extraResource: 'resources/backend.jar',
        ignore: filePath => !isPackaged(filePath)
    },
    rebuildConfig: {},
    makers: [
        {
            name: '@electron-forge/maker-squirrel',
            config: {
                setupIcon: 'src/assets/icon.ico'
            },
        },
        {
            name: '@electron-forge/maker-zip',
            platforms: ['darwin'],
        },
        {
            name: '@electron-forge/maker-deb',
            config: {},
        },
        {
            name: '@electron-forge/maker-rpm',
            config: {},
        },
    ],
    plugins: [
        {
            name: '@electron-forge/plugin-auto-unpack-natives',
            config: {},
        },
    ],
};
