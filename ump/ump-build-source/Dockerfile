FROM ubuntu:14.04

MAINTAINER QuangNL
USER root

# Install git
RUN echo "deb http://archive.ubuntu.com/ubuntu trusty main universe" >> /etc/apt/sources.list && \
apt-get update -y && \
apt-get install -y git

# Install Oracle Java 8
RUN apt-get install software-properties-common python-software-properties && \
add-apt-repository ppa:openjdk-r/ppa && \
apt-get update -y && \
apt-get install -y openjdk-8-jdk openjdk-8-jre && \
export JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64/ 

# Clean Up APT when finished
RUN apt-get clean && rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/*

# Update dpkg repositories and install wget
RUN apt-get update -y && \
apt-get install -y wget

# Get maven 3.3.9
RUN wget --no-verbose -O /tmp/apache-maven-3.3.9-bin.tar.gz http://www-eu.apache.org/dist/maven/maven-3/3.3.9/binaries/apache-maven-3.3.9-bin.tar.gz

# Install maven
RUN tar xzf /tmp/apache-maven-3.3.9-bin.tar.gz -C /opt/ && \
ln -s /opt/apache-maven-3.3.9 /opt/maven && \
ln -s /opt/maven/bin/mvn /usr/local/bin && \
rm -f /tmp/apache-maven-3.3.9-bin.tar.gz
ENV MAVEN_HOME /opt/maven
