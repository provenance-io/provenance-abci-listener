# Quick Start

Developers can use checkout, `gradle` and make targets, `make clean build run-config`,
`./build/provenance config set streaming.abci.plugin abci_v1` and `make run`
to run a local Provenance development network with state listening enabled.

<!-- TOC -->
  - [Prerequisites](#prerequisites)
  - [Build a local distribution](#build-a-local-distribution)
  - [Stand up the Confluent Platform](#stand-up-the-confluent-platform)
  - [Start a local isolated Provenance node](#start-a-local-isolated-provenance-node)
  - [Viewing the Data](#viewing-the-data)



## Prerequisites
- Git
- Go
    - comes with Make
- Docker
- Docker compose
- JDK 11+

## Build a local distribution

Create a workspace:
```shell
mkdir $HOME/workspace && export WORKSPACE=$HOME/workspace && cd $WORKSPACE
```

Clone repo:
```shell
git clone https://github.com/provenance-io/provenance-abci-listener  && cd provenance-abci-listener
```

Build distribution:
```shell
./gradlew assembleDist
```

Extract distribution:
```shell
cd build/distributions && unzip provenance-abci-listener-0-SNAPSHOT.zip
```

Export plugin for our node find:
```shell
export COSMOS_SDK_ABCI_V1=$WORKSPACE/provenance-abci-listener/build/distributions/provenance-abci-listener-0-SNAPSHOT/bin/provenance-abci-listener
```

## Stand up the Confluent Platform

If this is your first time standing up the Confluent Platform with `docker-compose` then follow
the [Quick Start for Confluent Platform](https://docs.confluent.io/platform/current/platform-quickstart.html#quick-start-for-cp).

```shell
cd $WORKSPACE
```

Download `cp-all-in-one` docker compose yaml:
```shell
curl --silent --output docker-compose.yml \
  https://raw.githubusercontent.com/confluentinc/cp-all-in-one/7.3.0-post/cp-all-in-one/docker-compose.yml
```

Start the Confluent Platform:
```shell
docker-compose up -d &> /dev/null
```

## Start a local isolated Provenance node

The quickest way to test the plugin with the Provenance blockchain is to start a local isolated node.

```shell
cd $WORKSPACE
```

Clone the repo:
```shell
git clone https://github.com/provenance-io/provenance  && cd provenance
```

Initialize local node:
```shell
make clean build run-config
```

Enable streaming (updates `build/run/config/app.toml`):
```shell
./build/provenanced config set streaming.abci.plugin abci_v1
```

Start local node:
```shell
make run
```

If all went well, you should see something like the below in the first few lines of the log output.
```shell
11:14AM INF streaming service registered plugin=abci_v1 service=abci
```

## Viewing the Data

To view the data, navigate to Confluent Platform UI http://localhost:9021 and look for the topics below.
It takes the `control-center` a minute or two to start up.
If it is not yet running, you can start it with `docker-compose restart control-center`.

```shell
local-listen-begin-block
local-listen-end-block
local-listen-deliver-tx # will not exist until the first Tx is sent
local-listen-commit
```
