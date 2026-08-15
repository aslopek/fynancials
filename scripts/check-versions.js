#!/usr/bin/env node

const fs = require('fs');
const path = require('path');

const root = path.join(__dirname, '..');

const clientPackageJsonPath = path.join(root, 'traquity-client-angular', 'package.json');
const serverPomPath = path.join(root, 'traquity-server-spring', 'pom.xml');
const clientOpenapitoolsPath = path.join(root, 'traquity-client-angular', 'openapitools.json');
const apiOpenapitoolsPath = path.join(root, 'traquity-api', 'openapitools.json');

function readJson(filePath) {
  return JSON.parse(fs.readFileSync(filePath, 'utf-8'));
}

function extractPomVersion(pomContent, artifactId) {
  const regex = new RegExp(`<artifactId>${artifactId}</artifactId>\\s*<version>([^<]+)</version>`);
  const match = pomContent.match(regex);
  return match ? match[1] : null;
}

function checkGroup(name, sources) {
  const values = new Set(sources.map(source => source.value));
  if (values.size === 1) {
    console.log(`${name}: ${[...values][0]} (in sync)`);
    return true;
  }

  console.error(`${name}: mismatch`);
  for (const source of sources) {
    console.error(`  ${source.label}: ${source.value}`);
  }
  return false;
}

function main() {
  const pomContent = fs.readFileSync(serverPomPath, 'utf-8');
  const clientPackageJson = readJson(clientPackageJsonPath);
  const clientOpenapitools = readJson(clientOpenapitoolsPath);
  const apiOpenapitools = readJson(apiOpenapitoolsPath);

  const productVersionOk = checkGroup('product version', [
    {label: 'traquity-client-angular/package.json', value: clientPackageJson.version},
    {label: 'traquity-server-spring/pom.xml', value: extractPomVersion(pomContent, 'traquity-server-spring')},
  ]);

  const generatorVersionOk = checkGroup('openapi-generator version', [
    {label: 'traquity-client-angular/openapitools.json', value: clientOpenapitools['generator-cli'].version},
    {label: 'traquity-api/openapitools.json', value: apiOpenapitools['generator-cli'].version},
    {label: 'traquity-server-spring/pom.xml', value: extractPomVersion(pomContent, 'openapi-generator-maven-plugin')},
  ]);

  if (!productVersionOk || !generatorVersionOk) {
    process.exit(1);
  }
}

main();
