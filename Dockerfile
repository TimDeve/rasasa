#
# GOLANG BUILDER
#
FROM golang:1.17-bullseye as golang-builder
LABEL builder=true

RUN mkdir -p /root/app

ADD ./gateway /root/app/gateway

WORKDIR /root/app/gateway

RUN go get -d -v ./...

RUN go build -o rasasa-gateway ./...

#
# NODE BUILDER
#
FROM node:16-bullseye-slim as node-builder
LABEL builder=true
RUN apt-get update \
 && apt-get install -y python build-essential \
 && rm -rf /var/lib/apt/lists/*

RUN npm install --location=global pnpm@8
ENV PNPM_HOME="/usr/local/share/pnpm"
ENV PATH="$PNPM_HOME:$PATH"
RUN bash -c "SHELL=/bin/bash pnpm setup" # Setups pnpm with bash because it really wants to be in add random stuff to .bashrc

RUN mkdir -p /root/app/client
WORKDIR /root/app/client

ADD client/.npmrc client/package.json client/pnpm-lock.yaml ./
RUN pnpm install --frozen-lockfile

ADD client .
RUN pnpm run lint
RUN NODE_ENV=production pnpm run build

#
# RUST BUILDER
#
FROM rust:1.67-slim-bullseye as rust-builder
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

RUN cargo new --bin server --name rasasa-server
WORKDIR ./server
ADD ./server/Cargo.lock ./server/Cargo.toml ./
RUN cargo build --release

ADD ./server .
RUN rm ./target/release/deps/rasasa_server*
RUN cargo build --release

#
# RUNNER
#
FROM node:16-bullseye-slim as runner

RUN apt-get update \
 && apt-get install -y git libpq5 \
 && rm -rf /var/lib/apt/lists/*

RUN npm install --location=global pnpm@8

RUN groupadd -r runner && useradd -m -r -g runner runner
USER runner

ENV PNPM_HOME="/home/runner/.local/share/pnpm"
ENV PATH="$PNPM_HOME:$PATH"
RUN bash -c "SHELL=/bin/bash pnpm setup" # Setups pnpm with bash because it really wants to be in add random stuff to .bashrc

RUN pnpm install --global concurrently

RUN mkdir -p /home/runner/app/read-server
WORKDIR /home/runner/app/read-server

ADD read-server/.npmrc read-server/package.json read-server/pnpm-lock.yaml ./
RUN pnpm install --frozen-lockfile --prod

ADD ./read-server .
WORKDIR /home/runner/app

# ADD ./Procfile .
COPY --from=node-builder /root/app/client/dist ./public
COPY --from=rust-builder /root/app/server/target/release/rasasa-server .
COPY --from=golang-builder /root/app/gateway/rasasa-gateway .

ENV GATEWAY_URL http://:8090
ENV SERVER_URL http://localhost:8091
ENV READ_URL http://localhost:8092

EXPOSE 8090
CMD concurrently -n 'Gateway,Server,Read' -c 'yellow,cyan,magenta' --kill-others './rasasa-gateway' './rasasa-server' '(cd read-server && pnpm start)'

