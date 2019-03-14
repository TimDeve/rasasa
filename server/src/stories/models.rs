use chrono::offset::FixedOffset;
use chrono::DateTime;

#[derive(Serialize)]
pub struct Story {
    pub title: String,
    pub url: String,
    #[serde(rename = "publishedDate")]
    pub published_date: DateTime<FixedOffset>,
    #[serde(rename = "isRead")]
    pub is_read: bool,
}
