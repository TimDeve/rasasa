ALTER TABLE stories
  DROP CONSTRAINT stories_feed_id_fkey;

ALTER TABLE stories
  ADD CONSTRAINT stories_feed_id_fkey FOREIGN KEY (feed_id) REFERENCES feeds(id);
