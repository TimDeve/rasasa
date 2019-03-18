extern crate chrono;
extern crate diesel;
extern crate http;
extern crate rss;
extern crate tokio;

pub mod models;

use diesel::insert_into;

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

#[derive(Extract)]
struct GetStoriesQueryString {
    refresh: Option<bool>,
}

impl_web! {
    impl StoriesResource {
        #[get("/v0/stories")]
        #[content_type("json")]
        fn get_news(&self, query_string: GetStoriesQueryString) -> Result<StoriesResponse, ()> {
            use crate::schema::stories::dsl::*;
            use crate::schema::feeds::dsl::*;

            let connection = establish_db_connection();

            if let Some(refresh) = query_string.refresh {
                if refresh {
                    let results = feeds
                        .load::<Feed>(&connection)
                        .expect("Error loading feeds");

                    let stories_list: Vec<NewStory> = results.iter()
                        .flat_map(|feed| fetch_stories(&feed.url, feed.id).unwrap())
                        .collect();

                    insert_into(stories)
                        .values(stories_list)
                        .on_conflict_do_nothing()
                        .execute(&connection).unwrap();
                }
            }


            let results = stories
                .order(published_date.desc())
                .load::<Story>(&connection)
                .expect("Error loading stories");

            Ok(StoriesResponse {
                stories: results
            })
        }

    }
}
