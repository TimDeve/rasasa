#[macro_use]
extern crate tower_web;
#[macro_use]
extern crate diesel;
#[macro_use]
extern crate diesel_migrations;
#[macro_use]
extern crate log;
extern crate chrono;
extern crate clokwerk;
extern crate env_logger;
extern crate http;
extern crate openssl;
extern crate rss;
extern crate tokio;

pub mod config;
pub mod feeds;
pub mod helpers;
mod scheduler;
pub mod schema;
pub mod stories;

use diesel::r2d2::{ConnectionManager, Pool};
use diesel::PgConnection;
use dotenv::dotenv;
use std::env;
use tower_web::middleware::log::LogMiddleware;
use tower_web::ServiceBuilder;

use crate::feeds::FeedsResource;
use crate::stories::StoriesResource;
use config::Config;
use scheduler::{run_startup_jobs, setup_scheduler};

embed_migrations!("migrations");

pub fn main() {
    dotenv().ok();

    env_logger::init();

    let database_url = env::var("DATABASE_URL").expect("DATABASE_URL env_var must be set");
    let manager = ConnectionManager::<PgConnection>::new(database_url);
    let pool = Pool::new(manager).unwrap();

    let connection = pool.get().unwrap();
    embedded_migrations::run_with_output(&connection, &mut std::io::stdout()).unwrap();

    let _thread_handle = setup_scheduler(pool.clone());
    run_startup_jobs(pool.clone());

    let addr = "127.0.0.1:8091".parse().expect("Invalid address");
    println!("Listening on http://{}", addr);

    ServiceBuilder::new()
        .config(Config { pool })
        .resource(FeedsResource)
        .resource(StoriesResource)
        .middleware(LogMiddleware::new("rasasa-server::web"))
        .run(&addr)
        .unwrap();
}
