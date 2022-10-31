DELETE FROM stories a
      USING stories b
WHERE a.id < b.id
  AND a.published_date = b.published_date
  AND a.url   = b.url;

ALTER TABLE stories ADD CONSTRAINT stories_url_published_date_uniq UNIQUE(url, published_date);
