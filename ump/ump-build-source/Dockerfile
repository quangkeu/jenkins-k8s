FROM ubuntu:14.04

MAINTAINER QuangNL
USER root

# Install git
RUN echo "deb http://archive.ubuntu.com/ubuntu trusty main universe" >> /etc/apt/sources.list && \
apt-get update -y && \
apt-get install -y git

# Install Oracle Java 8
ENV JAVA_VER 8
ENV JAVA_HOME /usr/lib/jvm/java-8-oracle

RUN echo 'deb http://ppa.launchpad.net/webupd8team/java/ubuntu trusty main' >> /etc/apt/sources.list && \
    echo 'deb-src http://ppa.launchpad.net/webupd8team/java/ubuntu trusty main' >> /etc/apt/sources.list && \
    apt-key adv --keyserver keyserver.ubuntu.com --recv-keys C2518248EEA14886 && \
    apt-get update && \
    echo oracle-java${JAVA_VER}-installer shared/accepted-oracle-license-v1-1 select true | /usr/bin/debconf-set-selections && \
    apt-get install -y --force-yes --no-install-recommends oracle-java${JAVA_VER}-installer oracle-java${JAVA_VER}-set-default && \
    apt-get clean && \
    rm -rf /var/cache/oracle-jdk${JAVA_VER}-installer

# Set Oracle Java as the default Java
RUN update-java-alternatives -s java-8-oracle

RUN echo "export JAVA_HOME=/usr/lib/jvm/java-8-oracle" >> ~/.bashrc

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
