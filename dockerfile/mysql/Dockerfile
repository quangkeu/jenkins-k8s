FROM admin/mongo

# Install MySQL
#RUN ufw disable
RUN echo "mysql-server mysql-server/root_password select root" | debconf-set-selections
RUN echo "mysql-server mysql-server/root_password_again select root" | debconf-set-selections
RUN apt-get install -y  mysql-server-5.5
RUN sudo sed -i 's/127\.0\.0\.1/0\.0\.0\.0/g' /etc/mysql/my.cnf
RUN sudo service mysql restart
RUN sleep 3s

# Define mountable directories.
VOLUME ["/etc/mysql", "/var/lib/mysql"]

# Define working directory.
#WORKDIR /data

# Define default command.
#CMD ["mysqld_safe"]

# Expose ports.
EXPOSE 3306

#RUN mysql -uroot -proot -e "CREATE DATABASE ump"
