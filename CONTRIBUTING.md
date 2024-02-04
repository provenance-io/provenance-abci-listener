# Preface

The provenance-abci-listener repository is built on the work of many open source projects including
the [Cosmos SDK]](https://github.com/cosmos/cosmos-sdk).

This project would not be possible without the dedication and support of the [Cosmos](https://cosmos.network) community.

# Contributing

<!-- TOC 2 4 -->
  - [Pull Requests](#pull-requests)
    - [PR Requirements](#pr-requirements)
    - [Process for reviewing PRs](#process-for-reviewing-prs)
  - [Forking](#forking)
  - [Dependencies](#dependencies)
  - [Protobuf](#protobuf)
  - [Testing](#testing)
  - [Branching Model](#branching-model)
    - [Branches and Tags](#branches-and-tags)
    - [Development Branch naming](#development-branch-naming)
    - [PR Targeting](#pr-targeting)
    - [Development Procedure](#development-procedure)
  - [Release Procedure](#release-procedure)
    - [Creating a Release](#creating-a-release)
      - [1. Create a .x Branch if Needed](#1-create-a-x-branch-if-needed)
      - [2. Update Changelog and Release Notes](#2-update-changelog-and-release-notes)
      - [3. Create the New Version Tag](#3-create-the-new-version-tag)
      - [4. PR the .x Branch Back to Main](#4-pr-the-x-branch-back-to-main)


Thank you for considering making contributions to the Provenance!

Contributing to this repo can mean many things such as participated in
discussion or proposing code changes. To ensure a smooth workflow for all
contributors, the general procedure for contributing has been established:

1. Either [open](https://github.com/provenance-io/provenance-abci-listener/issues/new/choose) or
   [find](https://github.com/provenance-io/provenance-abci-listener/issues) an issue you'd like to help with
2. Participate in thoughtful discussion on that issue
3. If you would like to contribute:
   1. If the issue is a proposal, ensure that the proposal has been accepted
   2. Ensure that nobody else has already begun working on this issue. If they have,
      make sure to contact them to collaborate
   3. If nobody has been assigned for the issue and you would like to work on it,
      make a comment on the issue to inform the community of your intentions
      to begin work
   4. Follow standard Github best practices: fork the repo, branch from the
      HEAD of `main`, make some commits, and submit a PR to `main`
      - For core developers working on `provenance-io`, to ensure a clear
        ownership of branches, branches must be named with the convention
        `{moniker}/{issue#}-branch-name`
   5. Be sure to submit the PR in `Draft` mode submit your PR early, even if
      it's incomplete as this indicates to the community you're working on
      something and allows them to provide comments early in the development process
   6. When the code is complete it can be marked `Ready for Review`
   7. Be sure to include a relevant change log entry in the `Unreleased` section
      of `CHANGELOG.md` (see file for log format)

Note that for very small or blatantly obvious problems (such as typos) it is
not required to an open issue to submit a PR, but be aware that for more complex
problems/features, if a PR is opened before an adequate design discussion has
taken place in a github issue, that PR runs a high likelihood of being rejected.

Other notes:

- Looking for a good place to start contributing? How about checking out some
  [good first issues](https://github.com/provenance-io/provenance-abci-listener/issues?q=is%3Aopen+is%3Aissue+label%3A%22good+first+issue%22)

## Pull Requests

To accommodate review process we suggest that PRs are categorically broken up.
Ideally each PR addresses only a single issue. Additionally, as much as possible
code refactoring and cleanup should be submitted as a separate PRs from bugfixes/feature-additions.

Draft PRs can be used for preliminary feedback and to see the results of the Github action checks.
They can also be used to better indicate that you are working on an issue.

### PR Requirements

Before a PR can be merged:
- All commits must be signed.
- It must be up-to-date with `main`.
- It must be approved by two or more maintainers.
- It must must pass all required Github action checks.

The following are encouraged and may sometimes be required:
- All Github action checks pass (even the non-required ones).
- New Unit and/or integration tests have been written.
- Documentation has been updated (in `/docs`).
- Test code coverage increases.

### Process for reviewing PRs

When reviewing PRs please use the following review explanations:

- `LGTM` without an explicit approval means that the changes look good, but you haven't pulled down the code, run tests locally and thoroughly reviewed it.
- `Approval` through the GH UI means that you understand the code, documentation/spec is updated in the right places, you have pulled down and tested the code locally. In addition:
  - You must also think through anything which ought to be included but is not
  - You must think through whether any added code could be partially combined (DRYed) with existing code
  - You must think through any potential security issues or incentive-compatibility flaws introduced by the changes
  - Naming must be consistent with conventions and the rest of the codebase
  - Code must live in a reasonable location, considering dependency structures (e.g. not importing testing modules in production code, or including example code modules in production code).
  - if you approve of the PR, you are responsible for fixing any of the issues mentioned here and more
- If you sat down with the PR submitter and did a pairing review please note that in the `Approval`, or your PR comments.
- If you are only making "surface level" reviews, submit any notes as `Comments` without adding a review.

## Forking

To create a fork and work on a branch of it, I would:

- Create the fork on github, using the fork button.
- `git remote rename origin upstream`
- `git remote add origin git@github.com:rigeyrigerige/provenance.git`

Now `origin` refers to my fork and `upstream` refers to the `provenance-io/npm-publish` version.
So I can `git push -u origin main` to update my fork, and make pull requests to `provenance-io` from there.
Of course, replace `rigeyrigerige` with your git handle.

To pull in updates from the origin repo, run

- `git fetch upstream`
- `git rebase upstream/main` (or whatever branch you want)

Please don't make Pull Requests from a `main` branch.

## Dependencies

We use [Gradle](https://gradle.com) to manage dependency versions.

Since most dependencies are not under our control, a third party may break our build.

## Testing

The project uses [JUnit](https://junit.org) for testing.

## Branching Model

Provenance uses the [trunk-based development branching model](https://trunkbaseddevelopment.com/) and [semantic versioning](https://semver.org/) (`<major>.<minor>.<patch>`).

- The `major` version represents the version of the entire network. It will likely only change as part of a fork.
- The `minor` version represents the features available. Changing to a new `minor` version involves an upgrade governance proposal.
- The `patch` version represents features or fixes that can be added without affecting state.
- Release candidates include `-rc#` at the end where the numbering starts at 1 (e.g. `-rc1`). Release candidates may not be state compatible with their predecessors.

### Branches and Tags

Provenance uses the `main` branch for new features and fixes.
It is not guaranteed that `main` will be compatible with the current Provenance blockchain networks.

A `.x` releases branch is made for each minor version. E.g. `release/1.12.x`.
A tag is created for each release. E.g. `1.12.0`.
To get a specific version, check it out by tag. E.g. `git checkout 1.12.0 -b tag-1.12.0`.

Some older repos still use `master` instead of `main` but the two are treated the same way.

The `main` branch and all `release/*` branches are protected and can only be updated via PR.
Branch protection might not be set up in all repos, but those branches should always be treated as if they were protected.

- The latest state of development is on `main`.
- Using `--force` onto a protected branch is not allowed (except when reverting a broken commit, which should seldom happen).
- Protected branches must not fail `yarn test`.
- Protected branches should not fail any Github action checks.

### Development Branch naming

- In a main repo (e.g. [provenance](https://github.com/provenance-io/provenance-abci-listener)), the preferred branch name format is `<user>/<issue #>-<short description>`.
- In forked repos under the `provenance-io` organization (e.g. [provenance-io/cosmos-sdk](https://github.com/provenance-io/cosmos-sdk)), the preferred branch name format is `prov/<user>/<issue #>-<short description>`.

### PR Targeting

All changes should target `main`.
If a change is needed in a release branch, it should first be PRed to `main` then be cherry-picked and re-PRed to the release branch.

### Development Procedure

1. Assign the issue to yourself and mark it as "In Progress" in any projects the issue is assigned to.
2. Checkout `main` and make sure it's up-to-date. E.g. `git checkout main && git pull`.
3. Create a development branch for your work using the development branch name format defined above. E.g. `git checkout -b myuser/123-add-foo-feature`.
4. Make changes and commit them. The suggested commit message format is `[issue #]: <message>`. E.g. `git commit -m "[123]: Update changelog."`.
5. Push up your changes.
6. Make a PR (possibly as a draft).
7. Repeat steps 4 and 5 as needed.
8. Mark your PR as "Ready to Review" (unless it's already that way).
9. Once the PR is ready (approved and all checks pass), it should be merged using the "Squash and Merge" strategy.

## Release Procedure

Definitions:
- A "major release" is one where the 1st number in the version string is increased.
  It usually has minor and patch versions of `0`, but in some cases might not.
- A "minor release" is one where the 2nd number in the version string is increased.
  It usually has a patch version of `0`, but might not.
- A "patch release" is one where the 3rd number in the version string is increased.
- A "release candidate" is one that has `-rc#` at the end of the version string. These are usually not used for patch releases, but can be.
- A "full release" is a release that isn't a release candidate.
- A "`.x` branch" is a git branch with the format `release/#.#.x`.
- The primary Provenance Blockchain network is "`mainnet`".
- The Provenance Blockchain network used for testing and integration is "`testnet`".

Git tags should only be used for releases.
A release is automatically created by GitHub when a tag is pushed that has the format `#.#.#` (where `#` is a whole number of any length).
A release candidate is created if the tag has the format `#.#.#-rc#`.

Release tags are created on the `.x` branches. E.g. on `release/1.13.x`.

The release cycle generally follows this pattern:
1. Create a new release candidate.
2. Publish to Docker Hub.
3Release patch versions as needed.

In some cases, a release candidate is not created before creating a full version.

### Creating a Release

Summary:
1. Create a `.x` branch if needed.
2. Update changelog and release notes on the `.x` branch.
3. Create the new version tag.
4. PR the `.x` branch back to `main`.

#### 1. Create a .x Branch if Needed

If a `.x` branch does not yet exist for the desired minor version, one must be created now.

- In a main repo (e.g. [provenance](https://github.com/provenance-io/provenance-abci-listener)), the `.x` branch name format is `release/#.#.x`.

1. Start on `main` and make sure you're up-to-date, e.g. `git checkout main && git pull`.
2. Create the new `.x` branch, e.g. `git checkout -b release/1.13.x`.
3. Push it to Github, e.g. `git push`.

#### 2. Update Changelog and Release Notes

You will need to create a new development branch for this and PR it back to the `.x` branch.

The `CHANGELOG.md` on the `.x` branch must be updated to reflect the new release.

1. Run `make linkify`.
2. Add a horizontal rule and version section heading, e.g.
   ```plaintext
   ---
   
   ## [1.13.0](https://github.com/provenance-io/provenance-abci-listener/releases/tag/1.13.0) - 2022-10-04
   ```
   This usually goes immediately under the `## Unreleased` heading to indicate that all unreleased things are now released.
   There should be an empty line both above the `---` and below the new version header.
3. If going from a release candidate to a full release, the release candidate entries should all be combined into one entry for the full release.
4. Optionally add an extra paragraph or two with general new version information.
   This should go below the newly added version heading but above any subheadings (e.g. `### Improvements`).
5. Add a `### Full Commit History` section at the end of the new version section with links to diffs between versions. E.g.
   ```plaintext
   ### Full Commit History
   
   * https://github.com/provenance-io/provenance-abci-listener/compare/1.12.0...1.13.0
   ```
   Note that the three dot `...` diff is preferred over the two dot `..` one for these links.
   For release candidates `2` and above, include links from both the previously released version and the previous release candidate.
   This should be the last section before the `---` above the next version entry.

Now, create/update the `RELEASE_CHANGELOG.md`.
For release candidates above `2`, the existing `RELEASE_CHANGELOG.md` should be updated to include info about the new version at the top.
For full or `-rc1` releases, delete any existing `RELEASE_CHANGELOG.md` and start a new empty one.

1. Copy the lines from `CHANGELOG.md` starting with the new version header and ending on the blank line before the hr above the next version entry.
2. Paste them into the `RELEASE_CHANGELOG.md`.

Push up your changes and PR them to the `.x` branch.

#### 3. Create the New Version Tag

Do the following locally.

1. Navigate to your locally cloned repo.
2. Make sure you've got up-to-date repo info. E.g. `git fetch`.
3. Checkout the `.x` branch and make sure it's up-to-date. E.g. `git checkout release/1.13.x && git pull`.
4. Create and sign the tag. E.g. `git tag -s 1.13.0 -m "Release 1.13.0"`.
5. Push the branch. E.g. `git push`.
6. Push the tag. E.g. `git push origin 1.13.0`.

You can then monitor the Github actions for the repo and also watch for the new release page to be created.

#### 4. PR the .x Branch Back to Main

This PR should update the `CHANGELOG.md` and contain any changes applied to the `.x` branch but not yet in `main`.
It should NOT contain the `RELEASE_CHANGELOG.md` file.

Do the following locally.

1. Navigate to your locally cloned repo.
2. Check out the `main` branch and make sure it's up-to-date. E.g. `git checkout main && git pull`.
3. Check out the `.x` branch and make sure it's up-to-date. E.g. `git checkout release/1.13.x && git pull`.
4. Create a new development branch. E.g. `git checkout -b myuser/1.13.0-back-to-main`.
5. Remove the `RELEASE_CHANGELOG.md` file.
6. Update your branch with `main`. E.g. `git merge main`.
7. Make sure the `CHANGELOG.md` correctly indicates the contents of the new release and still contains any unreleased entries.
8. Address any other conflicts that might exist.
9. Create a PR from your branch targeting `main`.
