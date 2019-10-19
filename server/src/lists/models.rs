use crate::feeds::models::Feed;
use crate::schema::feed_lists;
use crate::schema::lists;

#[derive(Insertable, Deserialize, Debug)]
#[table_name = "lists"]
pub struct NewList {
    pub name: String,
}

#[derive(Identifiable, Associations, Serialize, Queryable, Debug)]
#[table_name = "lists"]
pub struct List {
    pub id: i32,
    pub name: String,
}

#[derive(Deserialize, Debug)]
#[serde(rename_all = "camelCase")]
pub struct NewFeedForList {
    pub feed_id: i32,
}

#[derive(Insertable, Debug)]
#[table_name = "feed_lists"]
pub struct NewFeedList {
    pub feed_id: i32,
    pub list_id: i32,
}

#[derive(Identifiable, Associations, Serialize, Queryable, Debug)]
#[belongs_to(List)]
#[belongs_to(Feed)]
#[table_name = "feed_lists"]
pub struct FeedList {
    pub id: i32,
    pub feed_id: i32,
    pub list_id: i32,
}

#[derive(Serialize, Debug)]
#[serde(rename_all = "camelCase")]
pub struct ListWithFeedIds {
    pub id: i32,
    pub name: String,
    pub feed_ids: Vec<i32>,
}
