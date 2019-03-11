pub mod models;
pub mod schema;

#[macro_use]
extern crate diesel;
#[macro_use]
extern crate tower_web;
extern crate dotenv;

use diesel::pg::PgConnection;
use diesel::prelude::*;
use dotenv::dotenv;
use std::env;

pub fn establish_db_connection() -> PgConnection {
    dotenv().ok();

    let database_url = env::var("DATABASE_URL").expect("DATABASE_URL env_var must be set");
    PgConnection::establish(&database_url).expect(&format!("Error connecting to {}", database_url))
}
