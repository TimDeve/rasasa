ALTER TABLE feed_lists
  DROP CONSTRAINT feed_lists_feed_id_fkey;

ALTER TABLE feed_lists
  ADD CONSTRAINT feed_lists_feed_id_fkey FOREIGN KEY (feed_id) REFERENCES feeds(id) ON DELETE CASCADE;

ALTER TABLE feed_lists
  DROP CONSTRAINT feed_lists_list_id_fkey;

ALTER TABLE feed_lists
  ADD CONSTRAINT feed_lists_list_id_fkey FOREIGN KEY (list_id) REFERENCES lists(id) ON DELETE CASCADE;
