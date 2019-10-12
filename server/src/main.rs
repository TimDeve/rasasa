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
use std::env;
use std::io;

use crate::stories::stories_config;
use actix_web::{middleware, App, HttpServer};
use scheduler::{run_startup_jobs, setup_scheduler};

embed_migrations!("migrations");

pub fn main() -> io::Result<()> {
    std::env::set_var("RUST_LOG", "actix_web=debug");
    env_logger::init();

    let database_url = env::var("DATABASE_URL").expect("DATABASE_URL env_var must be set");
    let manager = ConnectionManager::<PgConnection>::new(database_url);
    let pool = Pool::new(manager).unwrap();

    let connection = pool.get().unwrap();
    embedded_migrations::run_with_output(&connection, &mut std::io::stdout()).unwrap();

    let _thread_handle = setup_scheduler(pool.clone());
    run_startup_jobs(pool.clone());

    let addr = "127.0.0.1:8091";
    println!("Listening on http://{}", addr);

    HttpServer::new(move || {
        App::new()
            .data(pool.clone())
            .wrap(middleware::Logger::default())
            .configure(stories_config)
    })
    .bind(addr)?
    .run()
}
