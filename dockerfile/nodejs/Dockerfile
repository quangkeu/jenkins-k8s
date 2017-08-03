FROM 10.84.20.62:8082/admin/ubuntu1404-maven339-jdk8

MAINTAINER QuangNL

# Install nodejs v0.10.48 and npm
RUN apt-get update -y
RUN apt-get install -y curl
RUN curl -sL https://deb.nodesource.com/setup_0.10 | sudo -E bash -
RUN apt-get install -y nodejs

VOLUME ["/data"]

ADD . /data
#RUN cd /data && npm install

EXPOSE 8888

#RUN npm start
