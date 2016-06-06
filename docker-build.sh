#!/bin/sh

set -e

docker build -f Dockerfile.build -t engine/build .
docker run --name=builder engine/build
docker cp builder:/app/ROOT.war .
docker rm builder
docker build -f Dockerfile.run -t engine .
rm ROOT.war
