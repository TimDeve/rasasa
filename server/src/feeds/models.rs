use diesel::*;

use crate::schema::feeds;

#[derive(Insertable, Deserialize)]
#[table_name = "feeds"]
pub struct NewFeed {
    pub name: String,
    pub url: String,
}

#[derive(Identifiable, Associations, Queryable, Serialize, Debug)]
#[table_name = "feeds"]
pub struct Feed {
    pub id: i32,
    pub name: String,
    pub url: String,
}
