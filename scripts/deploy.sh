#!/usr/bin/env bash

#
# Download plugin distribution and plugin configuration template.
#

usage() {
cat << EOF
Deploy provenance-abci-listener plugin to $PIO_HOME/plugins/abci/<protocol-version>/kafka.

Usage: ./deploy.sh <tag>

arguments:
  tag       The plugin release version tag to deploy.
            https://github.com/provenance-io/provenance-abci-listener/tags

EOF

  exit 1;
}

TAG=$1
PLUGINS_HOME="$PIO_HOME/plugins"
PLUGIN_NAME=provenance-abci-listener
PLUGIN_DIR="$PLUGINS_HOME/$PLUGIN_NAME-$TAG"
RELEASE_URL="https://github.com/provenance-io/provenance-abci-listener/releases/download/$TAG/provenance-abci-listener-$TAG.zip"
CONFIG_URL="https://raw.githubusercontent.com/provenance-io/provenance-abci-listener/$TAG/src/main/resources/application.conf"

[[ -z "$TAG" ]] && usage;

# download release distribution
printf "\nDownloading release %s...\n" "$TAG"
curl --create-dirs -o "$PLUGIN_DIR.zip" -L "$RELEASE_URL"

# validate md5 checksum
echo "Validating md5 checksum..."
curl --create-dirs -o "$PLUGIN_DIR.zip.md5" -L "$RELEASE_URL.md5"
cd "$PLUGINS_HOME" || exit 1
md5sum -c "$PLUGIN_DIR.zip.md5" || exit 1

printf "\nExtracting release %s...\n" "$TAG"
unzip "$PLUGIN_DIR.zip" -d "$PLUGINS_HOME"

# download application.conf
printf "\nDownloading application.conf...\n"
curl --create-dirs -o "$PLUGIN_DIR/application.conf" -L "$CONFIG_URL"

# set up plugin OPTS
printf "\nSetting up plugin environment variables...\n"
export PROVENANCE_ABCI_LISTENER_OPTS="-Dconfig.file=$PLUGIN_DIR/application.conf"
printf "\nPROVENANCE_ABCI_LISTENER_OPTS=%s" "$PROVENANCE_ABCI_LISTENER_OPTS"
export COSMOS_SDK_ABCI_V1="$PLUGIN_DIR/bin/$PLUGIN_NAME"
printf "\nCOSMOS_SDK_ABCI_V1=%s\n" "$COSMOS_SDK_ABCI_V1"

# check plugin can run with current config
cat << EOF
Plugin deployed!

DO NOT FORGET TO UPDATE application.conf to match your environment.

  Producer config options, see: https://kafka.apache.org/documentation/#producerconfigs

  Confluent Cloud configuration:

    kafka-clients {
      # Producer configs
      # https://kafka.apache.org/documentation/#producerconfigs
      bootstrap.servers = "{{ BOOTSTRAP_SERVERS }}"
      acks = all
      enable.idempotence = true
      max.in.flight.requests.per.connection = 1
      linger.ms = 50
      max.request.size = 8388608
      key.serializer = org.apache.kafka.common.serialization.StringSerializer
      value.serializer = io.confluent.kafka.serializers.protobuf.KafkaProtobufSerializer

      # Required connection configs for Confluent Cloud
      ssl.endpoint.identification.algorithm=https
      sasl.mechanism=PLAIN
      sasl.jaas.config="org.apache.kafka.common.security.plain.PlainLoginModule required username=\"{{ CLUSTER_API_KEY }}\" password=\"'{{ CLUSTER_API_SECRET }}\";"
      security.protocol=SASL_SSL

      # Required connection configs for Confluent Cloud Schema Registry
      schema.registry.url="{{ SR_URL }}"
      basic.auth.credentials.source=USER_INFO
      basic.auth.user.info="{{ SR_API_KEY }}:{{ SR_API_SECRET }}"
    }

EOF
