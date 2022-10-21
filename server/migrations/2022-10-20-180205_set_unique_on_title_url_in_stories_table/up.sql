ALTER TABLE stories DROP CONSTRAINT stories_url_published_date_key;

DELETE FROM stories a
      USING stories b
WHERE a.id < b.id
  AND a.title = b.title
  AND a.url   = b.url;

ALTER TABLE stories ADD CONSTRAINT stories_title_url_uniq UNIQUE(title, url);
