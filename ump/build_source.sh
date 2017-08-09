#!/bin/sh

homeDir=$(pwd)
# Build ump-core
cd $homeDir/ump-core/ && mvn clean deploy

# Build ump-backend
cd $homeDir/ump-backend/ && mvn clean deploy

# Build ump-webapp
cd $homeDir/ump-webapp/ && mvn clean deploy
