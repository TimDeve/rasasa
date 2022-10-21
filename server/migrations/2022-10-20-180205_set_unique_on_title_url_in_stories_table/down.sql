ALTER TABLE stories DROP CONSTRAINT stories_title_url_uniq;
ALTER TABLE stories ADD CONSTRAINT stories_url_published_date_key UNIQUE(url, published_date);
