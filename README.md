
<div align="center">
<img src="./docs/logo.svg" alt="Provenance ABCI Listener"/>
</div>
<br/><br/>

# Provenance ABCI Listener

The purpose of this app is to provide a listening plugin for the [Provenance Blockchain][Provenance]
of realtime ABCI events and store those events in Kafka.

The plugin is designed solely to work with the Cosmos SDK [ADR-038 State Streaming](https://github.com/cosmos/cosmos-sdk/blob/main/docs/architecture/adr-038-state-listening.md)
plugin system built with [hashicorp/go-plugin]() and is not intended to be stood up as a standalone application.
For other plugin examples see [Cosmos SDK streaming](https://github.com/cosmos/cosmos-sdk/blob/main/streaming/README.md) docs.

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

## Table of Contents
<!-- TOC -->
  - [How to run](#how-to-run)
    - [Pre-built Docker images](#pre-built-docker-images)
    - [Manual setup](#manual-setup)
  - [Linting](#linting)

## How to run

The plugin is designed to stream Provenance blockchain ABCI events to Kafka and register the respective message
Protobuf schema with the Confluent Platform Schema Registry. Therefore, it requires the Confluent Platform (Confluent Cloud or self-managed)
instances of the platform. For local testing we can rely on `docker-compose` to set up the Confluent Platform
and a local isolated node instance of the Provenance blockchain. Follow the instructions below to see it in action.

### Pre-built Docker images
- Coming soon!

### Manual setup

Prerequisites:
- Git
- Go
  - comes with Make
- Docker
- Docker compose

Steps:
1. Standup Confluent Platform
2. Build and export the plugin
3. Start a local isolated node

#### 1. Stand up the Confluent Platform

If this is your first time standing up the Confluent Platform with `docker-compose` then follow
the [Quick Start for Confluent Platform](https://docs.confluent.io/platform/current/platform-quickstart.html#quick-start-for-cp).

```shell
curl --silent --output docker-compose.yml \
  https://raw.githubusercontent.com/confluentinc/cp-all-in-one/7.3.0-post/cp-all-in-one/docker-compose.yml
```
```shell
docker-compose up -d
```

Navigate to the Control Center at http://lcoalhost:9021. It takes the control center a few minutes to start up.
If after a while you are not able to access it, run `docker-compose restart control-center` and wait a few minutes.

#### 2. Build and export the plugin

```shell
git clone https://github.com/provenance-io/provenance-abci-listener && cd provenance-abci-listener
```

```shell
./gradlew clean jar
```
Note: the `jar` task has been overwritten to create a uber JAR. We'll *export* the node before we start
the node in the next step.

#### 3. Start a local isolated node

The quickest way to test the plugin with the Provenance blockchain is to start local isolated node.

```shell
git clone https://github.com/provenance-io/provenance  && cd provenance
```

This allows you to configure a single node in a local environment
```shell
make clean build run-config
```

Enable streaming by specifying a plugin version *(abci_v1 is the currently supported message protocol version)*
```shell
./build/provenanced config set streaming.abci.plugin abci_v1
```

Export the plugin so the provenance binary can find it.
```shell
export COSMOS_SDK_ABCI_V1="java -jar <path to>/provenance-abci-listener/build/libs/provenance-abci-listener-SNAPSHOT.jar"
```
Note: local builds default to the `SNAPSHOT` version.

Start the node
```shell
make run
```

When configured correctly, on the very first few lines of the output you should see something like:
```shell
11:14AM INF streaming service registered plugin=abci_v1 service=abci
```

Navigate to Confluent Platform UI http://localhost:9021 and look for the following topics:
```shell
local-listen-begin-block
local-listen-end-block
local-listen-deliver-tx # will not exist until the first Tx is sent
local-listen-commit
```
The default plugin configuration uses the prefix `local-`. You can override this by overriding the plugin's
`application.conf` file. See the [Build and export the plugin](#2-build-and-export-the-plugin) section.

## Linting
To install the Kotlin linter run:
```
brew install ktlint
```

In order to automatically lint/check for things that can't be autocorrected run:
```
ktlint -F "src/**/*.kt"
```
