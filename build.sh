#!/usr/bin/env bash
set -e

SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )

cd ${SCRIPT_DIR}
./gradlew clean
./gradlew clean build -PicebergKafkaConnect -x test
podman build -t quarkus-kafka-connect:latest . --file src/main/docker/Dockerfile.jvm

#(cd "${SCRIPT_DIR}/examples" && docker-compose up )