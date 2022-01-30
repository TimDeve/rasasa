#
# GOLANG BUILDER
#
FROM golang:1.14 as golang-builder
LABEL builder=true

RUN mkdir -p /root/app

ADD ./gateway /root/app/gateway

WORKDIR /root/app/gateway

RUN go get -d -v ./...

RUN go build -o rasasa-gateway ./...

#
# NODE BUILDER
#
FROM node:16-buster-slim as node-builder
LABEL builder=true
RUN apt-get update \
 && apt-get install -y python build-essential \
 && rm -rf /var/lib/apt/lists/*

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
FROM rust:1.57-slim-buster as rust-builder
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
FROM node:16-buster-slim as runner
RUN apt-get update \
 && apt-get install -y git libpq5 \
 && rm -rf /var/lib/apt/lists/*
RUN npm install -g concurrently
RUN mkdir /root/app
WORKDIR /root/app
ADD ./read-server ./read-server
WORKDIR ./read-server
RUN npm ci
WORKDIR /root/app
# ADD ./Procfile .
COPY --from=node-builder /root/app/client/dist ./public
COPY --from=rust-builder /root/app/server/target/release/rasasa-server .
COPY --from=golang-builder /root/app/gateway/rasasa-gateway .

ENV GATEWAY_URL http://:8090
ENV SERVER_URL http://localhost:8091
ENV READ_URL http://localhost:8092

EXPOSE 8090
CMD concurrently -n 'Gateway,Server,Read' -c 'yellow,cyan,magenta' --kill-others './rasasa-gateway' './rasasa-server' '(cd read-server && npm start)'

