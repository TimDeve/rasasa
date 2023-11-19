#!/usr/bin/env bash

set -Eeuo pipefail

mark_unread=$(cat << SQL
UPDATE stories SET is_read = false WHERE is_read = true;
SQL
)

psql -d "$DATABASE_URL" -c "$mark_unread"
