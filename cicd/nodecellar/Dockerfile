# Nodecellar Dockerfile

FROM node:6
MAINTAINER sonvn

USER root

RUN mkdir -p /usr/api
COPY . /usr/api
WORKDIR /usr/api
RUN npm install --production --nodedir=/usr/api/node-v6.11.1

ENV PORT 3000
EXPOSE $PORT

CMD ["npm", "start"]
