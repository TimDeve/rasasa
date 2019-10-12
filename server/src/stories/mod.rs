extern crate chrono;
extern crate diesel;
extern crate http;
extern crate rss;
extern crate tokio;

pub mod models;

use actix_web::{web, Error, HttpResponse};
use diesel::insert_into;
use diesel::r2d2::{ConnectionManager, Pool};
use futures::Future;

use crate::diesel::prelude::*;
use crate::feeds::models::*;
use crate::helpers::fetch_stories;
use crate::stories::models::*;

type DbPool = Pool<ConnectionManager<PgConnection>>;

#[derive(Debug, Deserialize)]
struct GetStoriesQueryString {
    refresh: Option<bool>,
}

#[derive(Debug, Deserialize)]
struct PatchStoryBody {
    #[serde(rename = "isRead")]
    is_read: Option<bool>,
}

#[derive(Serialize)]
struct StoriesResponse {
    stories: Vec<Story>,
}

#[derive(Deserialize)]
struct PatchStoriesBody(Vec<StoryUpdate>);

fn get_stories(
    query: web::Query<GetStoriesQueryString>,
    pool: web::Data<DbPool>,
) -> Result<Vec<Story>, diesel::result::Error> {
    use crate::schema::feeds::dsl::*;
    use crate::schema::stories::dsl::*;

    let conn: &PgConnection = &pool.get().unwrap();

    if query.refresh.unwrap_or(false) {
        let results = feeds.load::<Feed>(conn).expect("Error loading feeds");

        let stories_list: Vec<NewStory> = results
            .iter()
            .flat_map(|feed| fetch_stories(&feed.url, feed.id).unwrap())
            .collect();

        insert_into(stories)
            .values(stories_list)
            .on_conflict_do_nothing()
            .execute(conn)?;
    }

    Ok(stories
        .filter(is_read.eq(false))
        .order(published_date.desc())
        .load::<Story>(conn)?)
}

fn get_stories_handler(
    query: web::Query<GetStoriesQueryString>,
    pool: web::Data<DbPool>,
) -> impl Future<Item = HttpResponse, Error = Error> {
    web::block(move || get_stories(query, pool)).then(|res| match res {
        Ok(stories) => Ok(HttpResponse::Ok().json(StoriesResponse { stories })),
        Err(_) => Ok(HttpResponse::InternalServerError().into()),
    })
}

fn get_story(story_id: i32, pool: web::Data<DbPool>) -> Result<Story, diesel::result::Error> {
    use crate::schema::stories::dsl::*;

    let conn: &PgConnection = &pool.get().unwrap();

    Ok(stories.filter(id.eq(story_id)).first::<Story>(conn)?)
}

fn get_story_handler(
    story_id: web::Path<i32>,
    pool: web::Data<DbPool>,
) -> impl Future<Item = HttpResponse, Error = Error> {
    web::block(move || get_story(story_id.into_inner(), pool)).then(|res| match res {
        Ok(story) => Ok(HttpResponse::Ok().json(story)),
        Err(_) => Ok(HttpResponse::InternalServerError().into()),
    })
}

fn patch_stories(
    body: PatchStoriesBody,
    pool: web::Data<DbPool>,
) -> Result<Vec<Story>, diesel::result::Error> {
    let PatchStoriesBody(stories) = body;

    let conn: &PgConnection = &pool.get().unwrap();

    let updated_stories = stories
        .iter()
        .map(|item| item.save_changes(conn).unwrap())
        .collect();

    Ok(updated_stories)
}

fn patch_stories_handler(
    web::Json(body): web::Json<PatchStoriesBody>,
    pool: web::Data<DbPool>,
) -> impl Future<Item = HttpResponse, Error = Error> {
    web::block(move || patch_stories(body, pool)).then(|res| match res {
        Ok(stories) => Ok(HttpResponse::Ok().json(StoriesResponse { stories })),
        Err(_) => Ok(HttpResponse::InternalServerError().into()),
    })
}

fn patch_story(
    story_id: i32,
    is_read: Option<bool>,
    pool: web::Data<DbPool>,
) -> Result<Story, diesel::result::Error> {
    let story = StoryUpdate {
        id: story_id,
        is_read: is_read,
    };

    let conn: &PgConnection = &pool.get().unwrap();

    let updated_story = story.save_changes(conn)?;

    Ok(updated_story)
}

fn patch_story_handler(
    story_id: web::Path<i32>,
    web::Json(body): web::Json<PatchStoryBody>,
    pool: web::Data<DbPool>,
) -> impl Future<Item = HttpResponse, Error = Error> {
    web::block(move || patch_story(story_id.into_inner(), body.is_read, pool)).then(|res| match res
    {
        Ok(story) => Ok(HttpResponse::Ok().json(story)),
        Err(_) => Ok(HttpResponse::InternalServerError().into()),
    })
}

pub fn stories_config(cfg: &mut web::ServiceConfig) {
    cfg.service(
        web::resource("/v0/stories")
            .route(web::get().to_async(get_stories_handler))
            .route(web::patch().to_async(patch_stories_handler)),
    )
    .service(
        web::resource("/v0/stories/{id}")
            .route(web::get().to_async(get_story_handler))
            .route(web::patch().to_async(patch_story_handler)),
    );
}
