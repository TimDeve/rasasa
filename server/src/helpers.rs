use atom_syndication::Feed;
use chrono::DateTime;
use reqwest::blocking::get;
use rss::Channel;

use crate::stories::models::NewStory;

#[derive(Debug)]
pub enum FeedType {
    Rss(Channel),
    Atom(Feed),
    None,
}

pub fn fetch_stories(
    url: &String,
    feed_id: i32,
) -> Result<Vec<NewStory>, Box<dyn std::error::Error>> {
    let content = get(url)?.text()?;

    let feed = extract_feed(content);

    return match feed {
        FeedType::Rss(f) => Ok(marshal_rss_feed_into_stories(f, feed_id)),
        FeedType::Atom(f) => Ok(marshal_atom_feed_into_stories(f, feed_id)),
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

fn marshal_atom_feed_into_stories(feed: Feed, feed_id: i32) -> Vec<NewStory> {
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
