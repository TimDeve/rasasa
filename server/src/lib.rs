pub mod models;
pub mod schema;

#[macro_use]
extern crate diesel;
#[macro_use]
extern crate tower_web;
extern crate dotenv;
extern crate reqwest;

use self::models::*;
use atom_syndication::Feed;
use diesel::pg::PgConnection;
use diesel::prelude::*;
use dotenv::dotenv;
use rss::Channel;
use std::env;

#[derive(Debug)]
pub enum FeedType {
    Rss(Channel),
    Atom(Feed),
    None,
}

pub fn establish_db_connection() -> PgConnection {
    dotenv().ok();

    let database_url = env::var("DATABASE_URL").expect("DATABASE_URL env_var must be set");
    PgConnection::establish(&database_url).expect(&format!("Error connecting to {}", database_url))
}

pub fn fetch_stories(url: &String) -> Result<Vec<Story>, Box<std::error::Error>> {
    let content = reqwest::get(url)?.text()?;

    let feed = extract_feed(content);

    return match feed {
        FeedType::Rss(f) => Ok(marshal_rss_feed_into_stories(f)),
        FeedType::Atom(f) => Ok(marshal_atom_feed_into_stories(f)),
        FeedType::None => Ok(vec![]),
    };
}

pub fn extract_feed(s: String) -> FeedType {
    let try_rss = Channel::read_from(s.as_bytes());
    let try_atom = Feed::read_from(s.as_bytes());

    return match (try_rss, try_atom) {
        (Ok(feed), Ok(_)) => FeedType::Rss(feed),
        (Ok(feed), Err(_)) => FeedType::Rss(feed),
        (Err(_), Ok(feed)) => FeedType::Atom(feed),
        (Err(_), Err(_)) => FeedType::None,
    };
}

fn marshal_atom_feed_into_stories(feed: Feed) -> Vec<Story> {
    feed.entries()
        .iter()
        .map(|entry| Story {
            is_read: false,
            url: entry.links()[0].href().to_string(),
            title: entry.title().to_string(),
        })
        .collect()
}

fn marshal_rss_feed_into_stories(feed: Channel) -> Vec<Story> {
    feed.items()
        .iter()
        .map(|entry| Story {
            is_read: false,
            url: entry.link().unwrap().to_string(),
            title: entry.title().unwrap().to_string(),
        })
        .collect()
}
