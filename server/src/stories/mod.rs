extern crate chrono;
extern crate diesel;
extern crate http;
extern crate rss;
extern crate tokio;

pub mod models;

use crate::diesel::prelude::*;
use crate::feeds::models::*;
use crate::helpers::{establish_db_connection, fetch_stories};
use models::*;

#[derive(Clone, Debug)]
pub struct StoriesResource;

#[derive(Response)]
struct StoriesResponse {
    stories: Vec<Story>,
}

impl_web! {
    impl StoriesResource {

        #[get("/v0/stories")]
        #[content_type("json")]
        fn get_news(&self) -> Result<StoriesResponse, ()> {
            use crate::schema::feeds::dsl::*;

            let connection = establish_db_connection();
            let results = feeds
                .load::<Feed>(&connection)
                .expect("Error loading feeds");

            let mut stories: Vec<Story> = results.iter()
                .flat_map(|feed| fetch_stories(&feed.url).unwrap())
                .collect();

            stories.sort_by(|a,b| b.published_date.cmp(&a.published_date));

            Ok(StoriesResponse {
                stories
            })
        }

    }
}
