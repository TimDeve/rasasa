use crate::diesel::prelude::*;
use atom_syndication::Feed as AtomFeed;
use chrono::{DateTime, Duration, Utc};
use reqwest::blocking::get;
use rss::Channel;

use crate::stories::models::NewStory;
use crate::PgPooledConnection;

#[derive(Debug)]
pub enum FeedType {
    Rss(Channel),
    Atom(AtomFeed),
    None,
}

fn fetch_stories(url: &str, feed_id: i32) -> Result<Vec<NewStory>, Box<dyn std::error::Error>> {
    let content = get(url)?.text()?;

    let feed = extract_feed(content);

    return match feed {
        FeedType::Rss(f) => Ok(marshal_rss_feed_into_stories(f, feed_id)),
        FeedType::Atom(f) => Ok(marshal_atom_feed_into_stories(f, feed_id)),
        FeedType::None => Ok(vec![]),
    };
}

pub fn fetch_this_week_stories(
    url: &str,
    feed_id: i32,
) -> Result<Vec<NewStory>, Box<dyn std::error::Error>> {
    let seven_days_ago: DateTime<chrono::Utc> = Utc::now() - Duration::days(7);

    fetch_stories(url, feed_id).map(|stories| {
        stories
            .into_iter()
            .filter(|s| s.published_date >= seven_days_ago)
            .collect()
    })
}

pub fn extract_feed(s: String) -> FeedType {
    if let Ok(feed) = Channel::read_from(s.as_bytes()) {
        return FeedType::Rss(feed);
    } else if let Ok(feed) = AtomFeed::read_from(s.as_bytes()) {
        return FeedType::Atom(feed);
    }

    return FeedType::None;
}

fn marshal_atom_feed_into_stories(feed: AtomFeed, feed_id: i32) -> Vec<NewStory> {
    feed.entries()
        .iter()
        .map(|entry| {
            let published_date = match (entry.published(), entry.updated()) {
                (Some(date), _) => *date,
                (_, date) => *date,
            };

            let content = match entry.content() {
                Some(content) => content.value().unwrap_or(""),
                None => "",
            };

            NewStory {
                is_read: false,
                feed_id,
                content: content.to_string(),
                url: entry.links()[0].href().to_string(),
                title: entry.title().to_string(),
                published_date,
            }
        })
        .collect()
}

fn marshal_rss_feed_into_stories(feed: Channel, feed_id: i32) -> Vec<NewStory> {
    feed.items()
        .iter()
        .map(|entry| NewStory {
            is_read: false,
            feed_id,
            content: entry.description().unwrap_or("").to_string(),
            url: entry.link().unwrap().to_string(),
            title: entry.title().unwrap().to_string(),
            published_date: DateTime::parse_from_rfc2822(entry.pub_date().unwrap()).unwrap(),
        })
        .collect()
}

pub fn delete_old_stories(conn: PgPooledConnection) {
    use crate::schema::stories::dsl::*;
    use diesel::dsl::{now, IntervalDsl};

    let result = diesel::delete(stories.filter(created_at.lt(now - 14_i32.days()))).execute(&conn);

    match result {
        Ok(n) => info!("Number of old stories deleted: {}", n),
        Err(e) => error!("Failed to delete old stories.\n{:?}", e),
    }
}

pub fn fetch_new_stories(conn: PgPooledConnection) {
    use diesel::insert_into;
    use diesel::pg::upsert::*;

    use crate::feeds::models::Feed;
    use crate::schema::feeds::dsl::feeds;
    use crate::schema::stories::dsl::*;

    let results = feeds.load::<Feed>(&conn);

    match results {
        Err(e) => error!("Error loading feeds.\n{:?}", e),
        Ok(loaded_feeds) => {
            let stories_list: Vec<NewStory> = loaded_feeds
                .iter()
                .flat_map(|feed| fetch_this_week_stories(&feed.url, feed.id).unwrap_or(vec![]))
                .collect();

            let update_published_date = published_date.eq(excluded(published_date));

            let db_result = insert_into(stories)
                .values(stories_list)
                .on_conflict(on_constraint("stories_title_url_uniq"))
                .do_update()
                .set(update_published_date)
                .execute(&conn);

            match db_result {
                Ok(_) => info!("Fetched new stories"),
                Err(e) => error!("Failed to insert new stories in DB.\n{:?}", e),
            }
        }
    }
}
