#!/usr/bin/env bash
set -Eeuo pipefail

ssh dokkudk postgres:export rasasadb | pg_restore -d "$DATABASE_URL"

