use super::schema::feeds;

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
    pub is_read: bool,
    pub url: String,
    pub title: String,
}
