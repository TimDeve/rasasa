use super::schema::feeds;
use chrono::offset::FixedOffset;
use chrono::DateTime;

#[derive(Insertable, Extract)]
#[table_name = "feeds"]
pub struct NewFeed {
    pub name: String,
    pub url: String,
}

#[derive(Queryable, Serialize)]
pub struct Feed {
    pub id: i32,
    pub name: String,
    pub url: String,
}

#[derive(Serialize)]
pub struct Story {
    pub title: String,
    pub url: String,
    #[serde(rename = "publishedDate")]
    pub published_date: DateTime<FixedOffset>,
    #[serde(rename = "isRead")]
    pub is_read: bool,
}
