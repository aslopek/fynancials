export type ThirdPartyLicenseEntrySource = 'angular-bundle' | 'electron-shell' | 'runtime-note';

export type ThirdPartyLicenseEntry = {
  name: string
  version: string
  license: string | null
  licenseText: string | null
  noticeText: string | null
  source: ThirdPartyLicenseEntrySource
};

export type ThirdPartyLicensesFile = {
  generatedAt: string
  packages: ThirdPartyLicenseEntry[]
};
