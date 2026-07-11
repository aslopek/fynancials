#!/usr/bin/env node

const fs = require('fs');
const path = require('path');
const {execSync} = require('child_process');

const root = path.join(__dirname, '..');
const clientDir = path.join(root, 'fynancials-client-angular');
const clientPackageJsonPath = path.join(clientDir, 'package.json');
const serverPomPath = path.join(root, 'fynancials-server-spring', 'pom.xml');
const changelogPath = path.join(root, 'CHANGELOG.md');
const releaseNotesPath = path.join(root, 'release-notes.md');

function bumpVersion(currentVersion, bump) {
  const [major, minor, patch] = currentVersion.split('.').map(Number);
  if (bump === 'patch') {
    return `${major}.${minor}.${patch + 1}`;
  }
  if (bump === 'minor') {
    return `${major}.${minor + 1}.0`;
  }
  return currentVersion;
}

function bumpPomVersion(newVersion) {
  const pomContent = fs.readFileSync(serverPomPath, 'utf-8');
  const regex = /(<artifactId>fynancials-server-spring<\/artifactId>\s*<version>)([^<]+)(<\/version>)/;
  if (!regex.test(pomContent)) {
    throw new Error(`Could not find fynancials-server-spring version in ${serverPomPath}`);
  }
  fs.writeFileSync(serverPomPath, pomContent.replace(regex, `$1${newVersion}$3`));
}

function updateChangelog(newVersion) {
  const changelogContent = fs.readFileSync(changelogPath, 'utf-8');
  const unreleasedHeadingRegex = /^## \[Unreleased\][ \t]*\r?\n/m;
  const unreleasedMatch = changelogContent.match(unreleasedHeadingRegex);
  if (!unreleasedMatch) {
    throw new Error(`Could not find "## [Unreleased]" heading in ${changelogPath}`);
  }

  const afterHeadingIndex = unreleasedMatch.index + unreleasedMatch[0].length;
  const rest = changelogContent.slice(afterHeadingIndex);
  const nextHeadingMatch = rest.match(/^## /m);
  const unreleasedBodyEnd = nextHeadingMatch ? nextHeadingMatch.index : rest.length;
  const unreleasedBody = rest.slice(0, unreleasedBodyEnd).trim();

  const releaseNotesBody = unreleasedBody.length > 0 ? unreleasedBody : '### Changed\n\n- Dependency updates.';

  const releaseDate = new Date().toISOString().slice(0, 10);
  const newSection = `## [${newVersion}] - ${releaseDate}\n\n${releaseNotesBody}\n\n`;

  const updatedChangelog =
    changelogContent.slice(0, afterHeadingIndex) + '\n' + newSection + rest.slice(unreleasedBodyEnd);
  fs.writeFileSync(changelogPath, updatedChangelog);

  fs.writeFileSync(releaseNotesPath, `${releaseNotesBody}\n`);
}

function main() {
  const bump = process.argv[2];
  if (!['patch', 'minor', 'none'].includes(bump)) {
    console.error(`Usage: node scripts/prepare-release.js <patch|minor|none>`);
    process.exit(1);
  }

  const clientPackageJson = JSON.parse(fs.readFileSync(clientPackageJsonPath, 'utf-8'));
  const currentVersion = clientPackageJson.version;
  const newVersion = bumpVersion(currentVersion, bump);

  if (bump !== 'none') {
    execSync(`npm version ${newVersion} --no-git-tag-version`, {cwd: clientDir, stdio: 'inherit'});
    bumpPomVersion(newVersion);
  }

  updateChangelog(newVersion);

  console.log(`version=${newVersion}`);

  if (process.env.GITHUB_OUTPUT) {
    fs.appendFileSync(process.env.GITHUB_OUTPUT, `version=${newVersion}\n`);
  }
}

main();
