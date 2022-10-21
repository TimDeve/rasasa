#[macro_use]
extern crate serde;
#[macro_use]
extern crate diesel;
#[macro_use]
extern crate diesel_migrations;
#[macro_use]
extern crate log;
extern crate chrono;
extern crate clokwerk;
extern crate env_logger;
extern crate openssl;
extern crate rss;

pub mod feeds;
pub mod lists;
mod scheduler;
pub mod schema;
pub mod stories;

use actix_web::web::Data;
use actix_web::{middleware, App, HttpServer};
use diesel::r2d2::{ConnectionManager, Pool, PooledConnection};
use diesel::PgConnection;
use reqwest::Url;
use scheduler::setup_scheduler;
use std::env;
use std::io;

type PgPool = Pool<ConnectionManager<PgConnection>>;
type PgPooledConnection = PooledConnection<ConnectionManager<PgConnection>>;

embed_migrations!("migrations");

#[actix_web::main]
async fn main() -> io::Result<()> {
    std::env::set_var("RUST_LOG", "info,rasasa_server=info,actix_web=debug");
    env_logger::init();

    let server_addr = {
        let env = env::var("SERVER_URL").expect("SERVER_URL env_var must be set");
        let url = Url::parse(&env).expect("Couldn't not parse SERVER_URL");
        url.socket_addrs(|| None)
            .expect("Could not get address out of SERVER_URL")
    };

    let database_url = env::var("DATABASE_URL").expect("DATABASE_URL env_var must be set");
    let manager = ConnectionManager::<PgConnection>::new(database_url);
    let pool = Pool::new(manager).unwrap();

    let connection = pool.get().unwrap();
    embedded_migrations::run_with_output(&connection, &mut std::io::stdout()).unwrap();

    let _thread_handle = setup_scheduler(pool.clone());

    HttpServer::new(move || {
        App::new()
            .app_data(Data::new(pool.clone()))
            .wrap(middleware::Logger::default())
            .configure(stories::handlers::config)
            .configure(feeds::handlers::config)
            .configure(lists::handlers::config)
    })
    .bind(&*server_addr)?
    .run()
    .await
}
