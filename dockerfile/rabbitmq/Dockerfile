FROM admin/redis

# Install RabbitMQ
RUN echo "deb http://www.rabbitmq.com/debian testing main" >> /etc/apt/sources.list
RUN wget https://www.rabbitmq.com/rabbitmq-signing-key-public.asc
RUN apt-key add rabbitmq-signing-key-public.asc
RUN apt-key adv --keyserver hkps.pool.sks-keyservers.net --recv-keys 0x6B73A36E6026DFCA
RUN apt-get update
RUN apt-get install -y rabbitmq-server

EXPOSE 5672
EXPOSE 15672
