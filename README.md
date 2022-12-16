
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
  - [Status](#status)
  - [Table of Contents](#table-of-contents)
  - [How to run](#how-to-run)
    - [Manual setup](#manual-setup)
  - [Building the Plugin](#building-the-plugin)
  - [Testing](#testing)
  - [Linting](#linting)

## How to run

The plugin is designed to stream Provenance blockchain ABCI events to Kafka and register the
respective Protobuf message schema with the Confluent Schema Registry. For local testing we
can rely on [cp-all-in-one](https://github.com/confluentinc/cp-all-in-one/blob/7.3.0-post/cp-all-in-one/docker-compose.yml)
and `docker-compose` to set up the Confluent Platform on our local environment. For production
environments you can use Confluent Cloud or self-managed Confluent Platform.

### Manual setup

Prerequisites:
- Git
- Go
  - comes with Make
- Docker
- Docker compose

Steps:
1. Standup Confluent Platform
2. Start a local isolated node


#### 1. Stand up the Confluent Platform

If this is your first time standing up the Confluent Platform with `docker-compose` then follow
the [Quick Start for Confluent Platform](https://docs.confluent.io/platform/current/platform-quickstart.html#quick-start-for-cp).

```shell
mkdir $HOME/workspace && cd $HOME/workspace
```

```shell
mkdir confluentinc && cd confluentinc

curl --silent --output docker-compose.yml \
  https://raw.githubusercontent.com/confluentinc/cp-all-in-one/7.3.0-post/cp-all-in-one/docker-compose.yml
```
```shell
docker-compose up -d

# ignore docker output
docker-compose up -d &> /dev/null
```

#### 2. Start a local isolated Provenance node

The quickest way to test the plugin with the Provenance blockchain is to start a local isolated node.

```shell
cd $HOME/workspace

git clone https://github.com/provenance-io/provenance  && cd provenance
```

This allows you to configure a single node in a local environment
```shell
make clean build run-config
```

Create a `plugins/` folder for housing the plugin binary and any plugin configs.
```shell
mkdir build/run/provenanced/plugins && cd build/run/provenanced/plugins

PLUGIN_VERSION={release version}

curl -L --silent --output provenance-abci-listener-$PLUGIN_VERSION.zip \
  https://github.com/provenance-io/provenance-abci-listener/releases/download/$PLUGIN_VERSION/provenance-abci-listener-$PLUGIN_VERSION.zip

unzip provenance-abci-listener-$PLUGIN_VERSION.zip

export COSMOS_SDK_ABCI_V1=$HOME/workspace/provenance/build/run/provenanced/plugins/provenance-abci-listener/bin/provenance-abci-listener
```

Check that everything is in order so far.
```shell
sh -c $COSMOS_SDK_ABCI_V1
1|1|tcp|127.0.0.1:1234|grpc
```
If everything is in order you'll see the above output. Now `CTRL-C` to stop the plugin.

Note: by default, the built-in configuration options look for Kafka on `localhost:9092`
and the Schema Registry on `http://localhost:8081`. For local testing this is fine,
but for other environments you'll need to provide the plugin with an external
configuration file. You can do this by downloading, updating, exporting the config.

```shell
curl --silent --output application.conf \
  https://raw.githubusercontent.com/provenance-io/provenance-abci-listener/main/src/$PLUGIN_VERSION/resources/application.conf
 
# external configuration
export PROVENANCE_ABCI_LISTENER_OPTS="-Dconfig.file=$HOME/workspace/provenance/build/run/provenanced/plugins/application.conf"
```

Enable streaming by specifying a plugin version *(abci_v1 is the currently supported message protocol version)*
```shell
./build/provenanced config set streaming.abci.plugin abci_v1
```

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
The default plugin configuration uses the topic prefix `local-`. You can override this by setting
`kafka.producer.listen-topics.prefix` in `application.conf` file.

## Building the Plugin

To build the plugin from source, run:

```shell
git clone https://github.com/provenance-io/provenance-abci-listener && cd provenance-abci-listener
```
```shell
# will also run tests
./gradlew assembleDist
```
You'll find the distribution files in `build/distributions/`.

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
```
ktlint -F "src/**/*.kt"
```
