
FROM 10.84.20.62:8082/admin/ubuntu1404-maven339-jdk8

MAINTAINER QuangNL

ENV DEBIAN_FRONTEND=noninteractive

# Install nodejs v0.10.48 and npm
RUN apt-get update -y
RUN apt-get install -y curl
RUN curl -sL https://deb.nodesource.com/setup_0.10 | sudo -E bash -
RUN apt-get install -y nodejs

EXPOSE 8888

RUN \
  apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv 7F0CEB10 && \
  echo 'deb http://downloads-distro.mongodb.org/repo/debian-sysvinit dist 10gen' > /etc/apt/sources.list.d/mongodb.list && \
  apt-get update && \
  apt-get install -y mongodb-org

RUN mkdir -p /data/db
RUN echo "bind_ip = 0.0.0.0" >> /etc/mongodb.conf

# Define mountable directories.
VOLUME ["/data/db"]

# Define working directory.
#WORKDIR /data

# Define default command.
#CMD ["mongod"]

# Expose ports.
#   - 27017: process
#   - 28017: http
EXPOSE 27017
EXPOSE 28017

# Install MySQL
#RUN ufw disable
RUN echo "mysql-server mysql-server/root_password select root" | debconf-set-selections
RUN echo "mysql-server mysql-server/root_password_again select root" | debconf-set-selections
RUN apt-get install -y  mysql-server-5.5
RUN sudo sed -i 's/127\.0\.0\.1/0\.0\.0\.0/g' /etc/mysql/my.cnf
#RUN sudo service mysql restart
#RUN sleep 3s
RUN sudo service mysql restart && sleep 5s \
&& mysql -uroot -proot -e "CREATE DATABASE ump;"


# Define mountable directories.
#VOLUME ["/etc/mysql", "/var/lib/mysql"]

# Define working directory.
#WORKDIR /data

# Define default command.
#CMD ["mysqld_safe"]

# Expose ports.
EXPOSE 3306

#RUN mysql -uroot -proot -e "CREATE DATABASE ump"

# Install Redis
ENV REDIS_USER=redis \
    REDIS_DATA_DIR=/var/lib/redis \
    REDIS_LOG_DIR=/var/log/redis

RUN apt-get update && DEBIAN_FRONTEND=noninteractive apt-get install -y redis-server \
 && sed 's/^daemonize yes/daemonize no/' -i /etc/redis/redis.conf && sed 's/^bind 127.0.0.1/bind 0.0.0.0/' -i /etc/redis/redis.conf \
 && sed 's/^# unixsocket /unixsocket /' -i /etc/redis/redis.conf \
 && sed 's/^# unixsocketperm 755/unixsocketperm 777/' -i /etc/redis/redis.conf \
 && sed '/^logfile/d' -i /etc/redis/redis.conf && rm -rf /var/lib/apt/lists/*

EXPOSE 6379/tcp


# Install RabbitMQ
RUN echo "deb http://www.rabbitmq.com/debian testing main" >> /etc/apt/sources.list
RUN wget https://www.rabbitmq.com/rabbitmq-signing-key-public.asc
RUN apt-key add rabbitmq-signing-key-public.asc
RUN apt-key adv --keyserver hkps.pool.sks-keyservers.net --recv-keys 0x6B73A36E6026DFCA
RUN apt-get update
RUN apt-get install -y rabbitmq-server
RUN cd /usr/sbin
RUN rabbitmq-plugins enable rabbitmq_management
RUN cd /opt/ && git clone https://github.com/quangkeu/ssdc-genieacs.git
RUN service rabbitmq-server restart \
&& sleep 10s \
&& curl -S http://localhost:15672/cli/rabbitmqadmin > /usr/sbin/rabbitmqadmin
RUN chmod 777 /usr/sbin/rabbitmqadmin \
&& sleep 5s
#RUN rabbitmqadmin -q import /opt/ssdc-genieacs/rabbit.config
#RUN rabbitmqctl list_exchanges

#CMD /usr/sbin/rabbitmqadmin -q import /opt/ssdc-genieacs/rabbit.config
EXPOSE 5672
EXPOSE 15672

# Install Genienacs
RUN cd /opt/ && rm -rf ssdc-genieacs && git clone https://github.com/quangkeu/ssdc-genieacs.git
RUN mkdir -p /home/ubuntu && mkdir -p /home/ubuntu/DeployGenieacs/logs
RUN cd /opt/ssdc-genieacs \
&& tar -xzf genieacs.tar.gz \
&& mv /opt/ssdc-genieacs/genieacs /home/ubuntu/ \
&& mv excute_on_qa.sh /home/ubuntu
 
RUN chmod +x /opt/ssdc-genieacs/deploy_genieacs.sh \
&& chmod +x /opt/ssdc-genieacs/entrypoint.sh \
&& chmod +x /home/ubuntu/excute_on_qa.sh 

CMD ["./opt/ssdc-genieacs/entrypoint.sh"]
