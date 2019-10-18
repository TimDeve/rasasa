CREATE TABLE feed_lists (
  id SERIAL PRIMARY KEY,
  feed_id SERIAL REFERENCES feeds(id),
  list_id SERIAL REFERENCES lists(id)
)

