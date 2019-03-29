pub mod models;

use crate::config::Connection;
use crate::diesel::prelude::*;
use crate::feeds::models::*;

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
        fn get_feeds(&self, Connection(conn): Connection) -> Result<FeedsResponse, ()> {
            use crate::schema::feeds::dsl::*;

            let results = feeds
                .load::<Feed>(&conn)
                .expect("Error loading feeds");

            Ok(FeedsResponse {
                feeds: results
            })
        }

        #[post("/v0/feeds")]
        fn create_feed(&self, body: NewFeed, Connection(conn): Connection) -> Result<http::Response<&'static str>, ()> {
            use crate::schema::feeds;

            diesel::insert_into(feeds::table)
                .values(&body)
                .execute(&conn)
                .expect("Error saving new post");

            Ok(http::Response::builder()
                .status(201)
                .body("")
                .unwrap())
        }

        #[delete("/v0/feeds/:feed_id")]
        fn delete_feed(&self, feed_id: i32, Connection(conn): Connection) -> Result<http::Response<&'static str>, ()> {
            use crate::schema::feeds::dsl::*;

            diesel::delete(feeds.filter(id.eq(feed_id)))
                .execute(&conn)
                .expect("Error deleting feed");

            Ok(http::Response::builder()
                .status(200)
                .body("")
                .unwrap())
        }
    }
}
