#!/bin/sh
type=$1
echo "type = $type"
if [ "$type" = "webapp" ]
then
	sourceDir="/var/lib/jenkins/.m2/repository/vn/vnpt/ssdc/ump-webapp/1.0-SNAPSHOT/"
	deployDir="/home/ubuntu/DeployUMP/"
	filename="ump-webapp-1.0-SNAPSHOT.war"
	filelog="webapp.log"

else
	sourceDir="/var/lib/jenkins/.m2/repository/vn/vnpt/ssdc/ump-backend/1.0-SNAPSHOT/"
	deployDir="/home/ubuntu/DeployUMP/"
	filename="ump-backend-1.0-SNAPSHOT.jar"
	filelog="backend.log"

	#build ump-backend for mysql
	cd /var/lib/jenkins/workspace/ump_backend
	mvn liquibase:update
fi

destination_file_path=$deployDir$filename
source_file_path=$sourceDir$filename


#Check if source foder contains file that need copy
if [ -e $source_file_path ]
then

	#Check if destination forder contain file
	if [ -e $destination_file_path ]
		then
		echo $destination_file_path "is exists"
		echo "Delete old file"
		rm $destination_file_path	
	fi

	#Move file to directory
	echo "Move $filename file from $sourceDir to $deployDir directory."
	cp $sourceDir$filename $deployDir

	#Check if process needing turn on is running
	PID=$(pgrep -f $filename)	
	echo "PID =$PID"

	if [ "$PID" = "" ]
	then
                echo "The process of $filename is none of exist"

	else 
		echo "The process of $filename is exist"
		echo "Kill process : $PID"
		kill -9 $PID
	fi

	#Run file jar
	echo "run background: " $destination_file_path
	BUILD_ID=dontKillMe nohup java -jar $destination_file_path >> $deployDir"logs/"$filelog &

else 
	echo $source_file_path "is not exists"
	echo "Fail"
fi




