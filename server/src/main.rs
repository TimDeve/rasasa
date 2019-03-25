#[macro_use] extern crate tower_web;
#[macro_use] extern crate diesel;
#[macro_use] extern crate diesel_migrations;
extern crate chrono;
extern crate env_logger;
extern crate http;
extern crate openssl;
extern crate rss;
extern crate tokio;

pub mod feeds;
pub mod helpers;
pub mod schema;
pub mod stories;

use dotenv::dotenv;
use tower_web::middleware::log::LogMiddleware;
use tower_web::ServiceBuilder;

use crate::feeds::FeedsResource;
use crate::stories::StoriesResource;
use crate::helpers::establish_db_connection;

embed_migrations!("migrations");

pub fn main() {
    dotenv().ok();

    let _ = env_logger::try_init();

    let addr = "127.0.0.1:8091".parse().expect("Invalid address");
    println!("Listening on http://{}", addr);

    let connection = establish_db_connection();
    embedded_migrations::run_with_output(&connection, &mut std::io::stdout()).unwrap();

    ServiceBuilder::new()
        .resource(FeedsResource)
        .resource(StoriesResource)
        .middleware(LogMiddleware::new("rasasa-server::web"))
        .run(&addr)
        .unwrap();
}
