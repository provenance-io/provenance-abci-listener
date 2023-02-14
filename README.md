
<div align="center">
<img src="./docs/logo.svg" alt="Provenance ABCI Listener"/>
</div>
<br/><br/>

# Provenance ABCI Listener

The purpose of this app is to provide a listening plugin for the [Provenance Blockchain][Provenance]
of realtime ABCI events and store those events in Kafka.

The plugin is not designed to be stood up as a standalone application. It is designed to be loaded by the plugin system built into the Cosmos SDK.
Read [ADR-038 State Streaming](https://github.com/cosmos/cosmos-sdk/blob/main/docs/architecture/adr-038-state-listening.md) for details on the design.

## Status

[![Latest Release][release-badge]][release-latest]
[![Apache 2.0 License][license-badge]][license-url]
[![LOC][loc-badge]][loc-report]
[![Lint Status][lint-badge]][lint-report]

[license-badge]: https://img.shields.io/github/license/provenance-io/provenance-abci-listener.svg
[license-url]: https://github.com/provenance-io/provenance-abci-listener/blob/main/LICENSE
[release-badge]: https://img.shields.io/github/tag/provenance-io/provenance-abci-listener.svg
[release-latest]: https://github.com/provenance-io/provenance-abci-listener/releases/latest
[loc-badge]: https://tokei.rs/b1/github/provenance-io/provenance-abci-listener
[loc-report]: https://github.com/provenance-io/provenance-abci-listener
[lint-badge]: https://github.com/provenance-io/provenance-abci-listener/workflows/ktlint/badge.svg
[lint-report]: https://github.com/provenance-io/provenance-abci-listener/actions/workflows/ktlint.yml
[provenance]: https://provenance.io/#overview


## Quick Start

The plugin is designed to stream Provenance blockchain ABCI events to Kafka and register the
respective Protobuf message schema with the Confluent Schema Registry. For local testing follow
the [Quick Start](docs/quick-start.md) guide.

## Production

For production environments, follow the [Deploying Provenance ABCI Listener Plugin](docs/deploy.md) guide.

## Testing

To test the plugin, run:

```shell
./gradlew test
````

## Linting
To install the Kotlin linter, run:
```
brew install ktlint
```

In order to automatically lint/check for things that can't be autocorrected run:
```shell
ktlint -F "src/**/*.kt"
```

## Distribution

To build a distribution on your local environment, run:

```shell
./gradlew assembleDist
```