use crate::schema::feeds;
use diesel::*;

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