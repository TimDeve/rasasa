CREATE TABLE feeds (
  id SERIAL PRIMARY KEY,
  name TEXT NOT NULL,
  url TEXT NOT NULL,
  UNIQUE(url)
)
