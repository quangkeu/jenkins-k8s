#!/bin/sh

homeDir=$(pwd)
# Build ump-core
cd $homeDir/jenkins-k8s/ump/ump-core/ && mvn clean deploy

# Build ump-backend
cd $homeDir/jenkins-k8s/ump/ump-backend && mvn clean deploy

# Build ump-webapp
cd $homeDir/jenkins-k8s/ump/ump-webapp && mvn clean deploy
