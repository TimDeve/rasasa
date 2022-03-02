extern crate chrono;
extern crate diesel;
extern crate rss;

use actix_web::{web, Error, HttpResponse};
use diesel::insert_into;
use diesel::r2d2::{ConnectionManager, Pool};

use crate::diesel::prelude::*;
use crate::feeds::models::*;
use crate::helpers::fetch_stories;
use crate::lists::models::*;
use crate::stories::models::*;

type DbPool = Pool<ConnectionManager<PgConnection>>;

#[derive(Debug, Deserialize)]
#[serde(rename_all = "camelCase")]
struct GetStoriesQueryString {
    refresh: Option<bool>,
    read: Option<bool>,
    list_id: Option<i32>,
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
    use crate::schema::lists::dsl::*;
    use crate::schema::stories::dsl::*;

    let conn: &PgConnection = &pool.get().unwrap();

    if query.refresh.unwrap_or(false) {
        let feeds_list = feeds.load::<Feed>(conn).expect("Error loading feeds");

        let stories_list: Vec<NewStory> = feeds_list
            .iter()
            .flat_map(|feed| fetch_stories(&feed.url, feed.id).unwrap())
            .collect();

        insert_into(stories)
            .values(stories_list)
            .on_conflict_do_nothing()
            .execute(conn)?;
    }

    let mut boxed_stories = stories.into_boxed();

    if let Some(list_id) = query.list_id {
        use diesel::pg::expression::dsl::any;

        let list = lists.find(list_id).first::<List>(conn)?;

        let feed_ids = FeedList::belonging_to(&list)
            .select(crate::schema::feed_lists::feed_id)
            .load::<i32>(conn)?;

        boxed_stories = boxed_stories.filter(feed_id.eq(any(feed_ids)))
    }

    if !query.read.unwrap_or(false) {
        boxed_stories = boxed_stories.filter(is_read.eq(false))
    }

    Ok(boxed_stories
        .limit(500)
        .order(published_date.desc())
        .load(conn)?)
}

async fn get_stories_handler(
    query: web::Query<GetStoriesQueryString>,
    pool: web::Data<DbPool>,
) -> Result<HttpResponse, Error> {
    let res = web::block(move || get_stories(query, pool)).await?;

    match res {
        Ok(stories) => Ok(HttpResponse::Ok().json(StoriesResponse { stories })),
        Err(_) => Ok(HttpResponse::InternalServerError().into()),
    }
}

fn get_story(story_id: i32, pool: web::Data<DbPool>) -> Result<Story, diesel::result::Error> {
    use crate::schema::stories::dsl::*;

    let conn: &PgConnection = &pool.get().unwrap();

    Ok(stories.filter(id.eq(story_id)).first::<Story>(conn)?)
}

async fn get_story_handler(
    story_id: web::Path<i32>,
    pool: web::Data<DbPool>,
) -> Result<HttpResponse, Error> {
    let res = web::block(move || get_story(story_id.into_inner(), pool)).await?;

    match res {
        Ok(story) => Ok(HttpResponse::Ok().json(story)),
        Err(_) => Ok(HttpResponse::InternalServerError().into()),
    }
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

async fn patch_stories_handler(
    web::Json(body): web::Json<PatchStoriesBody>,
    pool: web::Data<DbPool>,
) -> Result<HttpResponse, Error> {
    let res = web::block(move || patch_stories(body, pool)).await?;

    match res {
        Ok(stories) => Ok(HttpResponse::Ok().json(StoriesResponse { stories })),
        Err(_) => Ok(HttpResponse::InternalServerError().into()),
    }
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

async fn patch_story_handler(
    story_id: web::Path<i32>,
    web::Json(body): web::Json<PatchStoryBody>,
    pool: web::Data<DbPool>,
) -> Result<HttpResponse, Error> {
    let res = web::block(move || patch_story(story_id.into_inner(), body.is_read, pool)).await?;

    match res {
        Ok(story) => Ok(HttpResponse::Ok().json(story)),
        Err(_) => Ok(HttpResponse::InternalServerError().into()),
    }
}

pub fn config(cfg: &mut web::ServiceConfig) {
    cfg.service(
        web::resource("/v0/stories")
            .route(web::get().to(get_stories_handler))
            .route(web::patch().to(patch_stories_handler)),
    )
    .service(
        web::resource("/v0/stories/{id}")
            .route(web::get().to(get_story_handler))
            .route(web::patch().to(patch_story_handler)),
    );
}
