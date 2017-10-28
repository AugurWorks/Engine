#!/bin/sh

VERSION=$(cat build.gradle | grep 'version = ' | cut -d' ' -f 3 | sed -e "s/'//" | sed -e "s/'//")
HASH=$(git rev-parse --short HEAD)

sed -i "s/$VERSION/$VERSION-$HASH/g" build.gradle