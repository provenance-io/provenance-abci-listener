 # Deploying Provenance ABCI Listener Plugin

<!-- TOC 3 -->
  - [Overview](#overview)
  - [Plugin Deployment](#plugin-deployment)
  - [Node Configuration](#node-configuration)


## Overview

This document outlines steps to deploy release distributions of the plugin.

## Deploy Plugin

Follow the steps below to download, configure and deploy the plugin.

1. **Specify release**

    ```shell
    TAG={{ RELEASE_TAG }}
    ```
    Release versions can be found [here](https://github.com/provenance-io/provenance-abci-listener/tags).


2. **Download**
    
    2.1 - Create directories

    ```shell
    mkdir -p $PIO_HOME/plugins
   ```

    2.2 - Download plugin

   ```shell
    curl -s https://raw.githubusercontent.com/provenance-io/provenance-abci-listener/main/scripts/deploy.sh | bash -s $TAG
    ```

    2.3 - Export plugin

    ```shell
    export $PIO_HOME/plugins/provenance-abci-listener-$TAG/bin/provenance-abci-listener
    ```

3. **Configure**

    3.1 - Self-managed Kafka and Confluent Schema Registry

    ```shell
    curl -o $PIO_HOME/plugins/application.conf \
      https://raw.githubusercontent.com/provenance-io/provenance-abci-listener/$TAG/src/main/resources/application.conf
    ```

    3.1.1 - Edit configuration and SET the following properties
    ```shell
    bootstrap.servers
    schema.registry.url
    ```

    3.2 - Confluent Cloud

    ```shell
    curl -o $PIO_HOME/plugins/application.conf \
      https://raw.githubusercontent.com/provenance-io/provenance-abci-listener/$TAG/src/main/resources/ccloud.conf
    ```

    3.2.2 - Edit configuration and REPLACE with your ccloud values
    ```shell
    {{ BOOTSTRAP_SERVER }}
    {{ CLOUD_API_KEY }}
    {{ CLOUD_API_SECRET }}
    {{ SR_ENDPOINT }}
    {{ SR_API_KEY }}
    {{ SR_API_SECRET }}
    ```

    3.3 Export application config

    ```shell
    export PROVENANCE_ABCI_LISTENER_OPTS="-Dconfig.file=$PIO_HOME/plugins/application.conf"
    ```

## Configure Node

1. **Enable plugin**

    ```shell
    provenanced config set streaming.abci.plugin abci_v1
    ```

2. **Enable state change listening**

    ```shell
    provenanced config set streaming.abci.keys '["*"]'
    ```
    * `'["*"]'` - captures state changes for **all** module stores
    * `'["metadata", "attribute", "bank", "gov"[,"..."]]'` - captures state changes for **specific** module stores


3. **Start Node**

    ```shell
    provenanced start --x-crisis-skip-assert-invariants --log_level=info
    ```
    * `trace|debug|info|warn|error|fatal|panic` - log level options (default is `info`)
