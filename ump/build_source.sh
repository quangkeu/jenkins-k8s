# Build source code

homeDir = $(pwd)

# Build ump-core first
cd $homeDir/ump-core/ && mvn clean deploy

# Build ump-backend
cd $homeDir/ump-backend/ && mvn clean deploy

# Build ump-webapp
cd $homeDir/ump-webapp/ && mvn clean deploy
