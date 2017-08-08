FROM ubuntu:14.04

MAINTAINER QuangNL

ENV DEBIAN_FRONTEND=noninteractive

# ---Install git---
RUN apt-get update 
RUN apt-get install -y git

# ---Update the package repository---
RUN echo "deb http://archive.ubuntu.com/ubuntu trusty main universe" > /etc/apt/sources.list
RUN apt-get -y update

# ---Install Oracle Java 8---
RUN apt-get install software-properties-common python-software-properties
RUN add-apt-repository ppa:openjdk-r/ppa
RUN apt-get update
RUN apt-get install openjdk-8-jdk openjdk-8-jre 
RUN export JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64/ 

# ---Clean Up APT when finished---
RUN apt-get clean && rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/*

# ---Update dpkg repositories---
RUN apt-get update 

# ---Install wget---
RUN apt-get install -y wget

# ---Get maven 3.3.9---
RUN wget --no-verbose -O /tmp/apache-maven-3.3.9-bin.tar.gz http://www-eu.apache.org/dist/maven/maven-3/3.3.9/binaries/apache-maven-3.3.9-bin.tar.gz

# ---Install maven---
RUN tar xzf /tmp/apache-maven-3.3.9-bin.tar.gz -C /opt/
RUN ln -s /opt/apache-maven-3.3.9 /opt/maven
RUN ln -s /opt/maven/bin/mvn /usr/local/bin
RUN rm -f /tmp/apache-maven-3.3.9-bin.tar.gz
ENV MAVEN_HOME /opt/maven


# ---Install nodejs v0.10.48 and npm---
RUN apt-get update -y
RUN apt-get install -y curl
RUN curl -sL https://deb.nodesource.com/setup_0.10 | sudo -E bash -
RUN apt-get install -y nodejs

# ---Install Mongodb---
RUN apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv 7F0CEB10 \
&& echo 'deb http://downloads-distro.mongodb.org/repo/debian-sysvinit dist 10gen' > /etc/apt/sources.list.d/mongodb.list \
&& apt-get update \
&& apt-get install -y mongodb-org
RUN mkdir -p /data/db
RUN echo "bind_ip = 0.0.0.0" >> /etc/mongodb.conf

# ---Install MySQL---
#RUN ufw disable
RUN echo "mysql-server mysql-server/root_password select root" | debconf-set-selections
RUN echo "mysql-server mysql-server/root_password_again select root" | debconf-set-selections
RUN apt-get install -y  mysql-server-5.5
RUN sudo sed -i 's/127\.0\.0\.1/0\.0\.0\.0/g' /etc/mysql/my.cnf
RUN sudo service mysql restart && sleep 10s \
&& mysql -uroot -proot -e "CREATE DATABASE ump;" 
#&& mysql -uroot -proot -e "CREATE USER 'root'@'%' IDENTIFIED BY 'root';" \
#&& mysql -uroot -proot -e "GRANT ALL PRIVILEGES ON *.* TO 'root'@'%';" \
#&& mysql -uroot -proot -e "FLUSH PRIVILEGES;"


# ---Install Redis---
ENV REDIS_USER=redis \
    REDIS_DATA_DIR=/var/lib/redis \
    REDIS_LOG_DIR=/var/log/redis

RUN apt-get update && DEBIAN_FRONTEND=noninteractive apt-get install -y redis-server \
 && sed 's/^bind 127.0.0.1/bind 0.0.0.0/' -i /etc/redis/redis.conf \
 && sed 's/^# unixsocket /unixsocket /' -i /etc/redis/redis.conf \
 && sed 's/^# unixsocketperm 755/unixsocketperm 777/' -i /etc/redis/redis.conf \
 && sed '/^logfile/d' -i /etc/redis/redis.conf && rm -rf /var/lib/apt/lists/*

# ---Install RabbitMQ---
RUN echo "deb http://www.rabbitmq.com/debian testing main" >> /etc/apt/sources.list
RUN wget https://www.rabbitmq.com/rabbitmq-signing-key-public.asc
RUN apt-key add rabbitmq-signing-key-public.asc
RUN apt-key adv --keyserver hkps.pool.sks-keyservers.net --recv-keys 0x6B73A36E6026DFCA
RUN apt-get update
RUN apt-get install -y rabbitmq-server python2.7 \
&& ln -s /usr/bin/python2.7 /usr/bin/python
RUN cd /usr/sbin
RUN rabbitmq-plugins enable rabbitmq_management
RUN service rabbitmq-server restart \
&& sleep 10s \
&& curl -S http://localhost:15672/cli/rabbitmqadmin > /usr/sbin/rabbitmqadmin \
&& chmod 777 /usr/sbin/rabbitmqadmin 
#&& rabbitmqadmin -q import /opt/ssdc-genieacs/rabbit.config
