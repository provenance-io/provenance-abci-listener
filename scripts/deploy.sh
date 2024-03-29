#!/usr/bin/env bash

#
# Download plugin distribution and extract the plugin.
# In addition, validate the md5 checksum of the file.
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
RELEASE_URL="https://github.com/provenance-io/provenance-abci-listener/releases/download/$TAG/provenance-abci-listener-$TAG.tar.gz"

[[ -z "$TAG" ]] && usage;

echo "Release: $TAG"

# download release distribution
echo "Downloading release..."
curl -s --create-dirs -o "$PLUGIN_DIR.tar.gz" -L "$RELEASE_URL"

# validate md5 checksum
echo "Validating release (md5)..."
curl -s --create-dirs -o "$PLUGIN_DIR.tar.gz.md5" -L "$RELEASE_URL.md5"
cd "$PLUGINS_HOME" || exit 1
md5sum -c "$PLUGIN_DIR.tar.gz.md5" || exit 1

echo "Extracting release..."
tar -zxf "$PLUGIN_DIR.tar.gz" -C "$PLUGINS_HOME"
rm "$PLUGIN_DIR.tar.gz"
