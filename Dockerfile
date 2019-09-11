#
# GOLANG BUILDER
#
FROM golang:1.12 as golang-builder
LABEL builder=true

RUN mkdir -p /root/app

ADD ./gateway /root/app/gateway

WORKDIR /root/app/gateway

RUN go get -d -v ./...

RUN go build -o rasasa-gateway ./...

#
# NODE BUILDER
#
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
RUN NODE_ENV=production ./run client:build


#
# RUST BUILDER
#
FROM rust:1.37-stretch as rust-builder
LABEL builder=true

RUN mkdir -p /root/app
WORKDIR /root/app
RUN rustup target add x86_64-unknown-linux-musl
RUN apt-get update && \
      apt-get install -y \
      build-essential \
      cmake \
      curl \
      file \
      git \
      musl-dev \
      musl-tools \
      libpq-dev \
      libssl-dev \
      pkgconf \
      xutils-dev \
      ca-certificates

ADD run .
ADD ./scripts ./scripts

ADD ./server ./server
WORKDIR ./server
RUN cargo build

#
# RUNNER
#
FROM node:lts
RUN npm install -g concurrently
RUN mkdir /root/app
WORKDIR /root/app
ADD ./read-server ./read-server
WORKDIR ./read-server
RUN npm ci
WORKDIR /root/app
COPY --from=node-builder /root/app/client/dist ./public
COPY --from=rust-builder /root/app/server/target/debug/rasasa-server .
COPY --from=golang-builder /root/app/gateway/rasasa-gateway .
EXPOSE 8090
CMD concurrently -n 'Gateway,Server,Read' -c 'yellow,cyan,magenta' --kill-others './rasasa-gateway' 'RUST_LOG=info,rasasa-server=info ./rasasa-server' 'NODE_ENV=production node read-server/src/index.js'
