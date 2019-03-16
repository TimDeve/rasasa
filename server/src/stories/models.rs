use chrono::offset::FixedOffset;
use chrono::DateTime;
use diesel::*;

use crate::schema::stories;

#[derive(Insertable)]
#[table_name = "stories"]
pub struct NewStory {
    pub feed_id: i32,
    pub title: String,
    pub url: String,
    pub is_read: bool,
    pub published_date: DateTime<FixedOffset>,
}

#[derive(Serialize, Queryable)]
pub struct Story {
    pub id: i32,
    #[serde(skip_serializing)]
    pub feed_id: i32,
    pub title: String,
    pub url: String,
    #[serde(rename = "isRead")]
    pub is_read: bool,
    #[serde(rename = "publishedDate")]
    pub published_date: DateTime<chrono::Utc>,
}