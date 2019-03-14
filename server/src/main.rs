#[macro_use]
extern crate tower_web;
extern crate chrono;
#[macro_use]
extern crate diesel;
extern crate http;
extern crate rss;
extern crate tokio;

pub mod feeds;
pub mod helpers;
pub mod schema;
pub mod stories;

use feeds::FeedsResource;
use stories::StoriesResource;
use tower_web::ServiceBuilder;

pub fn main() {
    let addr = "127.0.0.1:8091".parse().expect("Invalid address");
    println!("Listening on http://{}", addr);

    ServiceBuilder::new()
        .resource(FeedsResource)
        .resource(StoriesResource)
        .run(&addr)
        .unwrap();
}
