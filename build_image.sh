#!/bin/sh

./gradlew bootJar

docker image build -t projecty-web .
