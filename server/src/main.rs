#[macro_use]
extern crate tower_web;
#[macro_use]
extern crate diesel;
#[macro_use]
extern crate diesel_migrations;
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

use diesel::r2d2::{ConnectionManager, Pool, PooledConnection};
use diesel::PgConnection;
use dotenv::dotenv;
use std::env;
use tower_web::extract::{Context, Extract, Immediate};
use tower_web::middleware::log::LogMiddleware;
use tower_web::util::BufStream;
use tower_web::ServiceBuilder;

use crate::feeds::FeedsResource;
use crate::stories::StoriesResource;

embed_migrations!("migrations");

struct Config {
    pool: Pool<ConnectionManager<PgConnection>>,
}

pub struct Connection(PooledConnection<ConnectionManager<PgConnection>>);

impl<B: BufStream> Extract<B> for Connection {
    type Future = Immediate<Connection>;

    fn extract(context: &Context) -> Self::Future {
        let config = context.config::<Config>().unwrap();
        Immediate::ok(Connection(config.pool.get().unwrap()))
    }
}

pub fn main() {
    dotenv().ok();

    let _ = env_logger::try_init();

    let database_url = env::var("DATABASE_URL").expect("DATABASE_URL env_var must be set");
    let manager = ConnectionManager::<PgConnection>::new(database_url);
    let pool = Pool::new(manager).unwrap();

    let connection = pool.get().unwrap();
    embedded_migrations::run_with_output(&connection, &mut std::io::stdout()).unwrap();

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
