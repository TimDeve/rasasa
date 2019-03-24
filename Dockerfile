FROM node:10.15-alpine as node-builder
LABEL builder=true

RUN apk add --no-cache --update \
      git \
      bash

RUN mkdir -p /root/app
WORKDIR /root/app

ADD run .
ADD ./scripts ./scripts
ADD ./client ./client
RUN ./run client:install:ci
RUN ./run client:build


FROM rust:1.33-stretch as rust-builder
LABEL builder=true

RUN mkdir -p /root/app
WORKDIR /root/app

ADD run .
ADD ./scripts ./scripts

ADD ./server ./server
RUN ./run server:check
WORKDIR ./server
RUN cargo build

FROM node:10.15-alpine
RUN mkdir /root/app
WORKDIR /root/app
RUN apk add --no-cache --update curl git bash
RUN curl https://getcaddy.com | bash -s personal
ADD ./read-server ./read-server
WORKDIR ./read-server
RUN npm ci
WORKDIR /root/app
COPY --from=node-builder /root/app/client/dist ./public
COPY --from=rust-builder /root/app/server/target/debug/rasasa-server .
ADD start-dist .
ADD Caddyfile .
EXPOSE 8090
CMD ["./start-dist"]
