# Changelog

All notable changes to Fynancials are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Changed

- Loading the Performance tab is significantly faster when prices need to be converted from a
  foreign currency (#6).

### Fixed

- Selecting a logo in the "Edit Security" dialog now shows the preview and enables the OK/Apply
  buttons (#3).
- Creating or updating a historical security price config now refreshes the security table and the
  depot position/performance data without requiring a reload (#4).
- Depot views now display monetary values in the depot's own currency instead of always EUR (#5).

## [1.0.0] - 2026-07-11

### Added

- Initial public release of Fynancials.

