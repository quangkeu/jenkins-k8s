#!/bin/sh

homeDir=$(pwd)
# Build ump-core
cd $homeDir/ump/ump-core/ && mvn clean deploy

# Build ump-backend
cd $homeDir/ump/ump-backend && mvn clean deploy

# Build ump-webapp
cd $homeDir/ump/ump-webapp && mvn clean deploy
