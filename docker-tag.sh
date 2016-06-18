#!/bin/sh

if [ -n "$1" ]; then
  echo "Tagging version $1"
  docker tag engine 274685854631.dkr.ecr.us-east-1.amazonaws.com/engine:$1
  docker tag engine 274685854631.dkr.ecr.us-east-1.amazonaws.com/engine:latest
  docker push 274685854631.dkr.ecr.us-east-1.amazonaws.com/engine:$1
  docker push 274685854631.dkr.ecr.us-east-1.amazonaws.com/engine:latest
else
  echo "No tag version provided"
  exit 1
fi
