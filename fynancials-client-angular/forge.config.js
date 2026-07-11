const path = require('path');
const fs = require('fs');
const {execSync} = require('child_process');
const {rimrafSync} = require('rimraf')

module.exports = {
    hooks: {
        generateAssets: async (config, buildPath, electronVersion, platform, arch) => {
            const fileName = `fynancials-server-spring-${require('./package.json').version}.jar`;
            const src = path.join(__dirname, '..', 'fynancials-server-spring', 'target', fileName);
            const resources = path.join(__dirname, 'resources');
            const dst = path.join(resources, 'backend.jar');

            rimrafSync(resources);
            fs.mkdirSync(resources);
            fs.cpSync(src, dst);
        },
        postPackage: async (forgeConfig, options) => {
            const nodeModules = [
                'custom-electron-prompt'
            ];
            let output = path.join(__dirname, 'out', `fynancials-${process.platform}-${process.arch}`,
                'resources', 'node_modules');
            if (process.platform === 'darwin') {
                output = path.join(__dirname, 'out', `fynancials-${process.platform}-${process.arch}`, 'fynancials.app',
                    'Contents', 'Resources', 'node_modules');
            }
            fs.mkdirSync(output, {
                recursive: true
            });
            let src, dst;

            for (const nodeModule of nodeModules) {
                src = path.join(__dirname, 'node_modules', nodeModule);
                dst = path.join(output, nodeModule);
                fs.cpSync(src, dst, {
                    recursive: true
                });
            }
          execSync(`npm --prefix ${output} prune --omit=dev`);
        }
    },
    packagerConfig: {
        asar: true,
        icon: 'src/assets/icon',
        extraResource: 'resources/backend.jar'
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
