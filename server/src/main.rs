#[macro_use]
extern crate tower_web;
extern crate diesel;
extern crate rasasa_server;
extern crate tokio;
extern crate http;

use self::diesel::prelude::*;
use rasasa_server::models::*;
use rasasa_server::establish_db_connection;
use tower_web::ServiceBuilder;

use http::Response;

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

impl_web! {
    impl App {
        #[get("/v0/name/:name")]
        #[content_type("json")]
        fn name_endpoint(&self, name: String) -> Result<NameResponse, ()> {
            Ok(NameResponse {
                name,
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
