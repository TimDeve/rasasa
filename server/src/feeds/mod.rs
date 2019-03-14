pub mod models;

use crate::diesel::prelude::*;
use crate::helpers::establish_db_connection;
use models::*;

#[derive(Clone, Debug)]
pub struct FeedsResource;

#[derive(Response)]
struct FeedsResponse {
    feeds: Vec<Feed>,
}

impl_web! {
    impl FeedsResource {

        #[get("/v0/feeds")]
        #[content_type("json")]
        fn get_feeds(&self) -> Result<FeedsResponse, ()> {
            use crate::schema::feeds::dsl::*;

            let connection = establish_db_connection();
            let results = feeds
                .load::<Feed>(&connection)
                .expect("Error loading feeds");

            Ok(FeedsResponse {
                feeds: results
            })
        }

        #[post("/v0/feeds")]
        fn create_feed(&self, body: NewFeed) -> Result<http::Response<&'static str>, ()> {
            use crate::schema::feeds;

            let connection = establish_db_connection();

            diesel::insert_into(feeds::table)
                .values(&body)
                .execute(&connection)
                .expect("Error saving new post");

            Ok(http::Response::builder()
                .status(201)
                .body("")
                .unwrap())
        }

    }
}
