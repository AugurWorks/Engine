#!/bin/sh

dos2unix application.properties
version=`cat application.properties | grep "^app.version=" | sed -r 's/.*app.version=(.*).*$/\1/'`

echo "Building container v$version$1"
docker build -t engine .

echo "Tagging version $version$1"
docker tag engine 274685854631.dkr.ecr.us-east-1.amazonaws.com/engine:$version$1
docker tag engine 274685854631.dkr.ecr.us-east-1.amazonaws.com/engine:latest$1
#docker push 274685854631.dkr.ecr.us-east-1.amazonaws.com/engine:$version$1
#docker push 274685854631.dkr.ecr.us-east-1.amazonaws.com/engine:latest$1
