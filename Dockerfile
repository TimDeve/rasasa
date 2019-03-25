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
RUN ./run client:build


#
# RUST BUILDER
#
FROM rust:1.33-stretch as rust-builder
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
RUN cargo build --target x86_64-unknown-linux-musl

#
# RUNNER
#
FROM node:10.15-alpine
RUN mkdir /root/app
WORKDIR /root/app
RUN apk add --no-cache --update curl git bash libpq libc6-compat
RUN curl https://getcaddy.com | bash -s personal
ADD ./read-server ./read-server
WORKDIR ./read-server
RUN npm ci
WORKDIR /root/app
COPY --from=node-builder /root/app/client/dist ./public
COPY --from=rust-builder /root/app/server/target/x86_64-unknown-linux-musl/debug/rasasa-server .
ADD start-dist .
ADD Caddyfile .
EXPOSE 8090
CMD ["bash"]
