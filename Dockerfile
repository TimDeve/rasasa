#
# GOLANG BUILDER
#
FROM golang:1.13 as golang-builder
LABEL builder=true

RUN mkdir -p /root/app

ADD ./gateway /root/app/gateway

WORKDIR /root/app/gateway

RUN go get -d -v ./...

RUN go build -o rasasa-gateway ./...

#
# NODE BUILDER
#
FROM node:lts-alpine as node-builder
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
RUN NODE_ENV=production ./run client:build


#
# RUST BUILDER
#
FROM rust:1.40-buster as rust-builder
LABEL builder=true

RUN mkdir -p /root/app
WORKDIR /root/app
RUN apt-get update && \
      apt-get install -y \
      build-essential \
      cmake \
      curl \
      file \
      git \
      libpq-dev \
      libssl-dev \
      pkgconf \
      xutils-dev \
      ca-certificates

ADD run .
ADD ./scripts ./scripts

ADD ./server ./server
WORKDIR ./server
RUN cargo build --release

#
# RUNNER
#
FROM node:lts-buster
RUN npm install -g concurrently
RUN mkdir /root/app
WORKDIR /root/app
ADD ./read-server ./read-server
WORKDIR ./read-server
RUN npm ci
WORKDIR /root/app
COPY --from=node-builder /root/app/client/dist ./public
COPY --from=rust-builder /root/app/server/target/release/rasasa-server .
COPY --from=golang-builder /root/app/gateway/rasasa-gateway .
EXPOSE 8090
RUN du -sh rasasa-server
CMD concurrently -n 'Gateway,Server,Read' \
                 -c 'yellow,cyan,magenta' \
                 --kill-others \
                 './rasasa-gateway' \
                 './rasasa-server' \
                 '(cd read-server && npm start)'
