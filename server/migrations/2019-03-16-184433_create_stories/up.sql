CREATE TABLE stories (
  id SERIAL PRIMARY KEY,
  feed_id INTEGER NOT NULL REFERENCES feeds(id),
  title TEXT NOT NULL,
  url TEXT NOT NULL,
  is_read BOOLEAN NOT NULL,
  published_date TIMESTAMPTZ NOT NULL,
  UNIQUE(url, published_date)
)

