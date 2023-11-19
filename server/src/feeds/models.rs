use diesel::*;

use crate::schema::feeds;

#[derive(Insertable, Deserialize)]
#[diesel(table_name = feeds)]
pub struct NewFeed {
    pub name: String,
    pub url: String,
}

#[derive(Identifiable, Queryable, Serialize, Debug)]
#[diesel(table_name = feeds)]
pub struct Feed {
    pub id: i32,
    pub name: String,
    pub url: String,
}
