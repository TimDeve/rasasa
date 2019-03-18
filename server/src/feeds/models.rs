use diesel::*;

use crate::schema::feeds;

#[derive(Insertable, Extract)]
#[table_name = "feeds"]
pub struct NewFeed {
    pub name: String,
    pub url: String,
}

#[derive(Identifiable, Associations, Queryable, Serialize)]
#[table_name = "feeds"]
pub struct Feed {
    pub id: i32,
    pub name: String,
    pub url: String,
}
