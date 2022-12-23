 # Deploying Provenance ABCI Listener Plugin

<!-- TOC 3 -->
  - [Overview](#overview)
  - [Deploying the Plugin](#deploying-the-plugin)
    - [Deployment](#deployment)
    - [Configuration](#configuration)
    - [Environment variables](#environment-variables)
    - [Node Configuration](#node-configuration)


## Overview

This document outlines steps to deploy release distributions of the plugin.

## Deploying the Plugin

We've created a script, `scrips/deploy.sh`, to help deploy the plugin into your Provenance node environment. The script needs to run from your node.

```shell
PLUGIN_VERSION={{ RELEASE_VERSION }}
```
Release versions can be found [here](https://github.com/provenance-io/provenance-abci-listener/tags).

### Deployment
```shell
curl --create-dirs -o $PIO_HOME/plugins/deploy.sh \
  https://raw.githubusercontent.com/provenance-io/provenance-abci-listener/$PLUGIN_VERSION/scripts/deploy.sh \
  && chmod +x $PIO_HOME/plugins/deploy.sh
```

```shell
sh $PIO_HOME/plugins/deploy.sh $PLUGIN_VERSION
```
Will deploy and extract the plugin to `$PIO_HOME/plugins/provenance-abci-listener-{version}`.

### Configuration

```shell
curl --create-dirs -o $PIO_HOME/plugins/provenance-abci-listener-$PLUGIN_VERSION/application.conf \
  https://raw.githubusercontent.com/provenance-io/provenance-abci-listener/$PLUGIN_VERSION/src/main/resources/application.conf
```

#### Alternatively
```hocon
cat << EOF >> $PIO_HOME/plugins/provenance-abci-listener-$PLUGIN_VERSION/application.conf
# Grpc server config
grpc.server {
  addr = localhost
  port = 1234
}

# Kafka producer config
kafka.producer {
  # Assign a topic name and optional prefix where events will be written.
  listen-topics {
    prefix = "local-"
    listen-begin-block = ${?kafka.producer.listen-topics.prefix}"listen-begin-block"
    listen-end-block = ${?kafka.producer.listen-topics.prefix}"listen-end-block"
    listen-deliver-tx = ${?kafka.producer.listen-topics.prefix}"listen-deliver-tx"
    listen-commit = ${?kafka.producer.listen-topics.prefix}"listen-commit"
  }

  # Properties defined by org.apache.kafka.clients.producer.ProducerConfig.
  # can be defined in this configuration section.
  kafka-clients {
    bootstrap.servers = "{{ BROKER_ENDPOINT }}"
    acks = all
    enable.idempotence = true
    max.in.flight.requests.per.connection = 1
    linger.ms = 50
    max.request.size = 204857600
    key.serializer = org.apache.kafka.common.serialization.StringSerializer
    value.serializer = io.confluent.kafka.serializers.protobuf.KafkaProtobufSerializer
    schema.registry.url ="http(s?)://{{ SR_ENDPOINT }}"
  }
}
EOF
```

#### Confluent Cloud Configuration

```hocon
cat << EOF >> $PIO_HOME/plugins/provenance-abci-listener-$PLUGIN_VERSION/application.conf
# Grpc server config
grpc.server {
  addr = localhost
  port = 1234
}

# Kafka producer config
kafka.producer {
  # Assign a topic name and optional prefix where events will be written.
  listen-topics {
    prefix = "local-"
    listen-begin-block = ${?kafka.producer.listen-topics.prefix}"listen-begin-block"
    listen-end-block = ${?kafka.producer.listen-topics.prefix}"listen-end-block"
    listen-deliver-tx = ${?kafka.producer.listen-topics.prefix}"listen-deliver-tx"
    listen-commit = ${?kafka.producer.listen-topics.prefix}"listen-commit"
  }

  # Properties defined by org.apache.kafka.clients.producer.ProducerConfig.
  # can be defined in this configuration section.
  kafka-clients {
    bootstrap.servers = "{{ BROKER_ENDPOINT }}"
    acks = all
    enable.idempotence = true
    max.in.flight.requests.per.connection = 1
    linger.ms = 50
    max.request.size = 204857600
    key.serializer = org.apache.kafka.common.serialization.StringSerializer
    value.serializer = io.confluent.kafka.serializers.protobuf.KafkaProtobufSerializer

    # Required connection configs for Confluent Cloud
    ssl.endpoint.identification.algorithm=https
    sasl.mechanism=PLAIN
    sasl.jaas.config="org.apache.kafka.common.security.plain.PlainLoginModule required username=\"{{ CLOUD_API_KEY }}\" password=\"{{ CLOUD_API_SECRET }}\";"
    security.protocol=SASL_SSL

    # Best practice for higher availability in Apache Kafka clients prior to 3.0
    session.timeout.ms=45000

    request.timeout.ms = 20000
    retry.backoff.ms = 500

    # Required connection configs for Confluent Cloud Schema Registry
    schema.registry.url="https://{{ SR_ENDPOINT }}"
    basic.auth.credentials.source=USER_INFO
    basic.auth.user.info="{{ SR_API_KEY }}:{{ SR_API_SECRET }}"
  }
}
EOF
```

### Environment variables

Let the plugin know about the Kafka configuration settings:
```shell
export PROVENANCE_ABCI_LISTENER_OPTS="-Dconfig.file=$PIO_HOME/plugins/provenance-abci-listener-$PLUGIN_VERSION/application.conf"
```

Let the node know where to find the plugin:
```shell
export COSMOS_SDK_ABCI_V1=$PIO_HOME/plugins/provenance-abci-listener-$PLUGIN_VERSION/bin/provenance-abci-listener
```

### Node Configuration

Enable ABCI streaming for your node
```shell
provenanced config set streaming.abci.plugin abci_v1
```

```shell
provenanced start --x-crisis-skip-assert-invariants --log_level=info
```
Logging level `trace|debug|info|warn|error|fatal|panic` (default "info")
