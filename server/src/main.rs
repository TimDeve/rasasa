#[macro_use]
extern crate tower_web;
extern crate chrono;
extern crate diesel;
extern crate http;
extern crate rasasa_server;
extern crate rss;
extern crate tokio;

use self::diesel::prelude::*;
use http::Response;
use rasasa_server::models::*;
use rasasa_server::*;
use tower_web::ServiceBuilder;

#[derive(Clone, Debug)]
struct App;

#[derive(Response)]
struct NameResponse {
    name: String,
}

#[derive(Response)]
struct FeedsResponse {
    feeds: Vec<Feed>,
}

#[derive(Response)]
struct StoriesResponse {
    stories: Vec<Story>,
}

impl_web! {
    impl App {
        #[get("/v0/name/:name")]
        #[content_type("json")]
        fn name_endpoint(&self, name: String) -> Result<NameResponse, ()> {
            Ok(NameResponse {
                name,
            })
        }

        #[get("/v0/stories")]
        #[content_type("json")]
        fn get_news(&self) -> Result<StoriesResponse, ()> {
            use rasasa_server::schema::feeds::dsl::*;

            let connection = establish_db_connection();
            let results = feeds
                .load::<Feed>(&connection)
                .expect("Error loading feeds");

            let mut stories :Vec<Story> = results.iter()
                .flat_map(|feed| fetch_stories(&feed.url).unwrap())
                .collect();

            stories.sort_by(|a,b| a.published_date.cmp(&b.published_date));

            Ok(StoriesResponse {
                stories
            })
        }

        #[get("/v0/feeds")]
        #[content_type("json")]
        fn get_feeds(&self) -> Result<FeedsResponse, ()> {
            use rasasa_server::schema::feeds::dsl::*;

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
            use rasasa_server::schema::feeds;

            let connection = establish_db_connection();

            diesel::insert_into(feeds::table)
                .values(&body)
                .execute(&connection)
                .expect("Error saving new post");

            Ok(Response::builder()
                .status(201)
                .body("")
                .unwrap())
        }
    }
}

pub fn main() {
    let addr = "127.0.0.1:8091".parse().expect("Invalid address");
    println!("Listening on http://{}", addr);

    ServiceBuilder::new().resource(App).run(&addr).unwrap();
}
