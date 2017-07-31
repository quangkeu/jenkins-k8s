#!/bin/sh
type=$1
echo "excute_on_qa:type = $type"
if [ "$type" = "webapp" ]
then
	deployDir="/home/ubuntu/DeployUMP/"
	filename="ump-webapp-1.0.war"
	filelog="webapp.log"
	
	#copy ump-webapp-1.0-SNAPSHOT.war file
	if [ ! -d /home/ubuntu/DeployUMP ]; then
	mkdir -p /home/ubuntu/DeployUMP
	fi
	cd /home/ubuntu/DeployUMP/
	wget http://10.84.12.190:8090/nexus/service/local/repositories/jenkin/content/vn/vnpt/ssdc/ump-webapp/1.0/ump-webapp-1.0.war

else
	deployDir="/home/ubuntu/DeployUMP/"
	filename="ump-backend-1.0.jar"
	filelog="backend.log"

	echo "start copying..."

	#copy ump-backend-1.0-SNAPSHOT.jar file
	# "ump@2016" is password of 10.84.20.138
	cd /home/ubuntu/DeployUMP/
	wget http://10.84.12.190:8090/nexus/service/local/repositories/jenkin/content/vn/vnpt/ssdc/ump-backend/1.0/ump-backend-1.0.jar
	#copy backend project to build liquibase
	mkdir -p /home/ubuntu/DeployUMP/source/ump-backend
     	mkdir -p /home/ubuntu/DeployUMP/source/ump-core
	


	cp /opt/ump-backend/pom.xml /home/ubuntu/DeployUMP/source/ump-backend
	cp -r  /opt/ump-backend/src /home/ubuntu/DeployUMP/source/ump-backend

	#copy ump-core for build backend project
	cp -r /opt/ump-core/src /home/ubuntu/DeployUMP/source/ump-core
	cp -r /opt/ump-core/target /home/ubuntu/DeployUMP/source/ump-core
	cp /opt/ump-core/pom.xml /home/ubuntu/DeployUMP/source/ump-core

	echo "copy complete"
	
	#buid ump-core project
	cd /home/ubuntu/DeployUMP/source/ump-core
	mvn clean install

	#buid ump-backend project for update database
	cd /home/ubuntu/DeployUMP/source/ump-backend
	mvn liquibase:update
fi

destination_file_path=$deployDir$filename


#Check if process needing turn on whether it is running
	PID=$(pgrep -f $filename)	
	echo "PID =$PID =>"

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
nohup java -jar $destination_file_path >> $deployDir"logs/"$filelog &
