use actix_web::{web, Error, HttpResponse};
use diesel::r2d2::{ConnectionManager, Pool};

use crate::diesel::prelude::*;
use crate::lists::models::*;
use crate::schema::*;

#[derive(Serialize)]
struct ListsResponse {
    lists: Vec<ListWithFeedIds>,
}

#[derive(Deserialize)]
struct DeleteFeedFromListRequest {
    list_id: i32,
    feed_id: i32,
}

type DbPool = Pool<ConnectionManager<PgConnection>>;

fn get_lists(pool: web::Data<DbPool>) -> Result<Vec<ListWithFeedIds>, diesel::result::Error> {
    use crate::schema::lists::dsl::*;

    let conn: &PgConnection = &pool.get().unwrap();

    lists
        .load::<List>(conn)?
        .into_iter()
        .map(|list| {
            let feed_ids = FeedList::belonging_to(&list)
                .select(feed_lists::feed_id)
                .load(conn)?;

            Ok(ListWithFeedIds {
                id: list.id,
                name: list.name,
                feed_ids,
            })
        })
        .collect()
}

async fn get_lists_handler(pool: web::Data<DbPool>) -> Result<HttpResponse, Error> {
    let res = web::block(move || get_lists(pool)).await?;

    match res {
        Ok(lists) => Ok(HttpResponse::Ok().json(ListsResponse { lists })),
        Err(_) => Ok(HttpResponse::InternalServerError().into()),
    }
}

fn create_list(new_list: NewList, pool: web::Data<DbPool>) -> Result<(), diesel::result::Error> {
    let conn: &PgConnection = &pool.get().unwrap();

    diesel::insert_into(lists::table)
        .values(&new_list)
        .execute(conn)?;

    Ok(())
}

async fn create_list_handler(
    web::Json(body): web::Json<NewList>,
    pool: web::Data<DbPool>,
) -> Result<HttpResponse, Error> {
    let res = web::block(move || create_list(body, pool)).await;

    match res {
        Ok(_) => Ok(HttpResponse::Created().body("")),
        Err(_) => Ok(HttpResponse::InternalServerError().into()),
    }
}

fn add_feed_to_list(
    feed_id: i32,
    list_id: i32,
    pool: web::Data<DbPool>,
) -> Result<(), diesel::result::Error> {
    let conn: &PgConnection = &pool.get().unwrap();

    diesel::insert_into(feed_lists::table)
        .values(&NewFeedList { feed_id, list_id })
        .execute(conn)?;

    Ok(())
}

async fn add_feed_to_list_handler(
    web::Json(body): web::Json<NewFeedForList>,
    list_id: web::Path<i32>,
    pool: web::Data<DbPool>,
) -> Result<HttpResponse, Error> {
    let res = web::block(move || add_feed_to_list(body.feed_id, list_id.into_inner(), pool)).await;

    match res {
        Ok(_) => Ok(HttpResponse::Created().body("")),
        Err(_) => Ok(HttpResponse::InternalServerError().into()),
    }
}

fn delete_feed_from_list(
    list_id: i32,
    feed_id: i32,
    pool: web::Data<DbPool>,
) -> Result<(), diesel::result::Error> {
    let conn: &PgConnection = &pool.get().unwrap();

    diesel::delete(
        feed_lists::table.filter(
            feed_lists::dsl::list_id
                .eq(list_id)
                .and(feed_lists::dsl::feed_id.eq(feed_id)),
        ),
    )
    .execute(conn)?;

    Ok(())
}

async fn delete_feed_from_list_handler(
    ids: web::Path<DeleteFeedFromListRequest>,
    pool: web::Data<DbPool>,
) -> Result<HttpResponse, Error> {
    let res = web::block(move || delete_feed_from_list(ids.list_id, ids.feed_id, pool)).await;

    match res {
        Ok(_) => Ok(HttpResponse::NoContent().body("")),
        Err(_) => Ok(HttpResponse::InternalServerError().into()),
    }
}

fn delete_list(list_id: i32, pool: web::Data<DbPool>) -> Result<(), diesel::result::Error> {
    let conn: &PgConnection = &pool.get().unwrap();

    diesel::delete(lists::table.filter(lists::dsl::id.eq(list_id))).execute(conn)?;

    Ok(())
}

async fn delete_list_handler(
    list_id: web::Path<i32>,
    pool: web::Data<DbPool>,
) -> Result<HttpResponse, Error> {
    let res = web::block(move || delete_list(list_id.into_inner(), pool)).await;

    match res {
        Ok(_) => Ok(HttpResponse::NoContent().body("")),
        Err(_) => Ok(HttpResponse::InternalServerError().into()),
    }
}

pub fn config(cfg: &mut web::ServiceConfig) {
    cfg.service(
        web::resource("/v0/lists")
            .route(web::get().to(get_lists_handler))
            .route(web::post().to(create_list_handler)),
    )
    .service(web::resource("/v0/lists/{id}").route(web::delete().to(delete_list_handler)))
    .service(web::resource("/v0/lists/{id}/feed").route(web::post().to(add_feed_to_list_handler)))
    .service(
        web::resource("/v0/lists/{list_id}/feed/{feed_id}")
            .route(web::delete().to(delete_feed_from_list_handler)),
    );
}
