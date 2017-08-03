FROM 10.84.20.62:8082/admin/ubuntu1404-maven339-jdk8

MAINTAINER QuangNL

# Install nodejs v0.10.48 and npm
RUN apt-get update -y
RUN curl -sL https://deb.nodesource.com/setup_0.10 | sudo -E bash -
RUN apt-get install -y nodejs

VOLUME ["/data"]

ADD . /data
RUN cd /data && npm install

EXPOSE 8888

RUN npm start

# Install MongoDB
RUN apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv 7F0CEB10
RUN echo 'deb http://downloads-distro.mongodb.org/repo/ubuntu-upstart dist 10gen' | sudo tee /etc/apt/sources.list.d/mongodb.list
RUN apt-get update
RUN apt-get install -y mongodb-org
RUN mkdir -p /data/db
RUN echo "bind_ip = 0.0.0.0" >> /etc/mongodb.conf

# Install MySQL
RUN ufw disable
RUN echo "mysql-server mysql-server/root_password select root" | debconf-set-selections
RUN echo "mysql-server mysql-server/root_password_again select root" | debconf-set-selections
RUN apt-get install -y  mysql-server-5.5 
RUN mysql -uroot -proot -e "CREATE DATABASE ump"

# Install Redis
ENV REDIS_USER=redis \
    REDIS_DATA_DIR=/var/lib/redis \
    REDIS_LOG_DIR=/var/log/redis

RUN apt-get update && DEBIAN_FRONTEND=noninteractive apt-get install -y redis-server \
 && sed 's/^daemonize yes/daemonize no/' -i /etc/redis/redis.conf && sed 's/^bind 127.0.0.1/bind 0.0.0.0/' -i /etc/redis/redis.conf \
 && sed 's/^# unixsocket /unixsocket /' -i /etc/redis/redis.conf \
 && sed 's/^# unixsocketperm 755/unixsocketperm 777/' -i /etc/redis/redis.conf \
 && sed '/^logfile/d' -i /etc/redis/redis.conf && rm -rf /var/lib/apt/lists/*

COPY entrypoint.sh /sbin/entrypoint.sh
RUN chmod 755 /sbin/entrypoint.sh

EXPOSE 6379/tcp
VOLUME ["${REDIS_DATA_DIR}"]
RUN ./sbin/entrypoint.sh

# Install RabbitMQ
RUN echo "deb http://www.rabbitmq.com/debian testing main" >> /etc/apt/sources.list
RUN wget https://www.rabbitmq.com/rabbitmq-signing-key-public.asc
RUN apt-key add rabbitmq-signing-key-public.asc
RUN apt-get update
RUN apt-get install -y rabbitmq-server

# Install Genienacs
RUN git clone https://github.com/quangkeu/jenkins-k8s.git
RUN cp -R ./jenkins-k8s/ump/ssdc-genieacs/ /opt/
RUN cd /opt/
RUN chmod +x ./ssdc-genieacs/deploy_genieacs.sh
RUN ./ssdc-genieacs/deploy_genieacs.sh


