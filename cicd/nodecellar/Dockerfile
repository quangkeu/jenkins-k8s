# Nodecellar Dockerfile

FROM node:6
MAINTAINER sonvn

RUN mkdir -p /usr/api
COPY . /usr/api
WORKDIR /usr/api
RUN npm install 

ENV PORT 3000
EXPOSE $PORT

CMD ["npm", "start"]
