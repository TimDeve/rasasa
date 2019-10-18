use actix_web::{web, Error, HttpResponse};
use diesel::r2d2::{ConnectionManager, Pool};
use futures::Future;

use crate::diesel::prelude::*;
use crate::lists::models::*;
use crate::schema::*;

#[derive(Serialize)]
struct ListsResponse {
    lists: Vec<ListWithFeedIds>,
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

fn get_lists_handler(pool: web::Data<DbPool>) -> impl Future<Item = HttpResponse, Error = Error> {
    web::block(move || get_lists(pool)).then(|res| match res {
        Ok(lists) => Ok(HttpResponse::Ok().json(ListsResponse { lists })),
        Err(_) => Ok(HttpResponse::InternalServerError().into()),
    })
}

pub fn config(cfg: &mut web::ServiceConfig) {
    cfg.service(web::resource("/v0/lists").route(web::get().to_async(get_lists_handler)));
}
