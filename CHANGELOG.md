<!--
Guiding Principles:

Changelogs are for humans, not machines.
There should be an entry for every single version.
The same types of changes should be grouped.
Versions and sections should be linkable.
The latest version comes first.
The release date of each version is displayed.
Mention whether you follow Semantic Versioning.

Usage:

Change log entries are to be added to the Unreleased section under the
appropriate stanza (see below). Each entry should ideally include a tag and
the GitHub issue reference in the following format:

* (<tag>) \#<issue-number> message

The issue numbers will later be link-ified during the release process so you do
not have to worry about including a link manually, but you can if you wish.

Types of changes (Stanzas):

"Features" for new features.
"Improvements" for changes in existing functionality.
"Deprecated" for soon-to-be removed features.
"Bug Fixes" for any bug fixes.
"Client Breaking" for breaking CLI commands and REST routes used by end-users.
"Data" for any data changes.
Ref: https://keepachangelog.com/en/1.0.0/
-->

## Unreleased

* nothing

---

## [1.0.0-rc3](https://github.com/provenance-io/provenance-abci-listener/releases/tag/1.0.0-rc3) - 2023-02-24

### Improvements

* (docs/ci) [PR 32](https://github.com/provenance-io/provenance-abci-listener/pull/31) Update deploy doc and scripts and mark pre-releases
* (docs) [PR 33](https://github.com/provenance-io/provenance-abci-listener/pull/33) Fix typos in deploy steps
* [PR 38](https://github.com/provenance-io/provenance-abci-listener/pull/38) Default to ByteArraySerializer when `schema.registry.url` property is not set

### Bug Fixes

* (ci) [PR 40](https://github.com/provenance-io/provenance-abci-listener/pull/40) Fix `PRE_RELEASE` environment variable in workflow

### Full Commit History

* https://github.com/provenance-io/provenance-abci-listener/compare/1.0.0-rc2...1.0.0-rc3

---

## [1.0.0-rc2](https://github.com/provenance-io/provenance-abci-listener/releases/tag/1.0.0-rc2) - 2023-02-09

### Improvements

* (ci) [PR 22](https://github.com/provenance-io/provenance-abci-listener/pull/) Updates CI workflows and bumps action versions

### Full Commit History

* https://github.com/provenance-io/provenance-abci-listener/compare/1.0.0-rc1...1.0.0-rc2

---

## [1.0.0-rc1](https://github.com/provenance-io/provenance-abci-listener/releases/tag/1.0.0-rc1) - 2023-02-08

### Improvements

* (docs/ci) [PR 32](https://github.com/provenance-io/provenance-abci-listener/pull/31) Update deploy doc and scripts and mark pre-releases
* (docs) [PR 33](https://github.com/provenance-io/provenance-abci-listener/pull/33) Fix typos in deploy steps
* [PR 38](https://github.com/provenance-io/provenance-abci-listener/pull/38) Default to ByteArraySerializer when `schema.registry.url` property is not set

### Bug Fixes

* (ci) [PR 40](https://github.com/provenance-io/provenance-abci-listener/pull/40) Fix `PRE_RELEASE` environment variable in workflow
---

## [1.0.0-rc2](https://github.com/provenance-io/provenance-abci-listener/releases/tag/1.0.0-rc2) - 2023-02-09

### Improvements

* (ci) [PR 22](https://github.com/provenance-io/provenance-abci-listener/pull/) Updates CI workflows and bumps action versions

### Full Commit History

* https://github.com/provenance-io/provenance-abci-listener/compare/1.0.0-rc1...1.0.0-rc2

---

## [1.0.0-rc1](https://github.com/provenance-io/provenance-abci-listener/releases/tag/1.0.0-rc1) - 2023-02-08

### Improvements

* (deps) [PR 5](https://github.com/provenance-io/provenance-abci-listener/pull/5)  Bump Provenance proto-kotlin to 1.14.0-rc2 (from [1.14.0-rc1](https://github.com/provenance-io/provenance/compare/v1.14.0-rc1...v1.14.0-rc2))
* [PR 6](https://github.com/provenance-io/provenance-abci-listener/pull/6) Update codeowners file

### Features

* [#2](https://github.com/provenance-io/provenance-abci-listener/issues/2) Add initial implementation of the gRPC State Listening plugin

### Bug Fixes

* [PR 8](https://github.com/provenance-io/provenance-abci-listener/pull/8) Fix release workflow to trigger on tagged versions
* [PR 12](https://github.com/provenance-io/provenance-abci-listener/pull/12) Fix release workflow to properly set distribution version

### Full Commit History

* https://github.com/provenance-io/provenance-abci-listener/compare/main...1.0.0-rc1

## PRE-HISTORY
