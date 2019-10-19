use chrono::offset::FixedOffset;
use chrono::{DateTime, NaiveDateTime};
use diesel::*;

use crate::feeds::models::Feed;
use crate::schema::stories;

#[derive(Insertable)]
#[table_name = "stories"]
pub struct NewStory {
    pub feed_id: i32,
    pub title: String,
    pub url: String,
    pub is_read: bool,
    pub content: String,
    pub published_date: DateTime<FixedOffset>,
}

#[derive(Identifiable, Associations, Serialize, Queryable, Debug)]
#[belongs_to(Feed, foreign_key = "feed_id")]
#[table_name = "stories"]
#[serde(rename_all = "camelCase")]
pub struct Story {
    pub id: i32,
    pub feed_id: i32,
    pub title: String,
    pub url: String,
    pub is_read: bool,
    pub published_date: DateTime<chrono::Utc>,
    #[serde(skip_serializing)]
    pub created_at: NaiveDateTime,
    pub content: String,
}

#[derive(AsChangeset, Identifiable, Deserialize, Debug)]
#[table_name = "stories"]
#[serde(rename_all = "camelCase")]
pub struct StoryUpdate {
    pub id: i32,
    pub is_read: Option<bool>,
}
