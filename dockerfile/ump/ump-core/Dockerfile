
FROM 10.84.20.62:8082/admin/ump-runtime

MAINTAINER QuangNL

# Install Genienacs
RUN cd /opt/ && git clone https://github.com/quangkeu/ssdc-genieacs.git \
&& mkdir -p /home/ubuntu && mkdir -p /home/ubuntu/DeployGenieacs/logs
RUN cd /opt/ssdc-genieacs \
&& tar -xzf genieacs.tar.gz \
&& mv /opt/ssdc-genieacs/genieacs /home/ubuntu/ \
&& mv execute_on_qa.sh /home/ubuntu/
 
RUN chmod +x /opt/ssdc-genieacs/deploy_genieacs.sh \
&& chmod +x /opt/ssdc-genieacs/entrypoint.sh \
&& chmod +x /home/ubuntu/execute_on_qa.sh 

CMD ["./opt/ssdc-genieacs/entrypoint.sh"]

EXPOSE 8080
