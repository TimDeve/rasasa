use actix_web::{web, Error, HttpResponse};
use diesel::r2d2::{ConnectionManager, Pool};

use crate::diesel::prelude::*;
use crate::feeds::models::*;

#[derive(Serialize)]
struct FeedsResponse {
    feeds: Vec<Feed>,
}

type DbPool = Pool<ConnectionManager<PgConnection>>;

fn get_feeds(pool: web::Data<DbPool>) -> Result<Vec<Feed>, diesel::result::Error> {
    use crate::schema::feeds::dsl::*;

    let conn: &PgConnection = &pool.get().unwrap();

    Ok(feeds.load::<Feed>(conn)?)
}

async fn get_feeds_handler(pool: web::Data<DbPool>) -> Result<HttpResponse, Error> {
    let res = web::block(move || get_feeds(pool)).await;

    match res {
        Ok(feeds) => Ok(HttpResponse::Ok().json(FeedsResponse { feeds })),
        Err(_) => Ok(HttpResponse::InternalServerError().into()),
    }
}

fn create_feed(body: NewFeed, pool: web::Data<DbPool>) -> Result<(), diesel::result::Error> {
    use crate::schema::feeds;

    let conn: &PgConnection = &pool.get().unwrap();

    diesel::insert_into(feeds::table)
        .values(&body)
        .execute(conn)?;

    Ok(())
}

async fn create_feed_handler(
    web::Json(body): web::Json<NewFeed>,
    pool: web::Data<DbPool>,
) -> Result<HttpResponse,Error> {
    let res = web::block(move || create_feed(body, pool)).await;

    match res {
        Ok(_) => Ok(HttpResponse::Created().body("")),
        Err(_) => Ok(HttpResponse::InternalServerError().into()),
    }
}

fn delete_feed(feed_id: i32, pool: web::Data<DbPool>) -> Result<(), diesel::result::Error> {
    use crate::schema::feeds::dsl::*;

    let conn: &PgConnection = &pool.get().unwrap();

    diesel::delete(feeds.filter(id.eq(feed_id))).execute(conn)?;

    Ok(())
}

async fn delete_feed_handler(
    list_id: web::Path<i32>,
    pool: web::Data<DbPool>,
) -> Result<HttpResponse, Error> {
    let res = web::block(move || delete_feed(list_id.into_inner(), pool)).await;

    match res {
        Ok(_) => Ok(HttpResponse::NoContent().body("")),
        Err(_) => Ok(HttpResponse::InternalServerError().into()),
    }
}

pub fn config(cfg: &mut web::ServiceConfig) {
    cfg.service(
        web::resource("/v0/feeds")
            .route(web::get().to(get_feeds_handler))
            .route(web::post().to(create_feed_handler)),
    )
    .service(web::resource("/v0/feeds/{id}").route(web::delete().to(delete_feed_handler)));
}
