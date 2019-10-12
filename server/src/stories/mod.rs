extern crate chrono;
extern crate diesel;
extern crate http;
extern crate rss;
extern crate tokio;

pub mod models;

use actix_web::{middleware, web, App, Error, HttpResponse, HttpServer};
use diesel::insert_into;
use diesel::r2d2::{ConnectionManager, Pool, PooledConnection};
use futures::future::{err, Either};
use futures::{Future, Stream};

use crate::config::Connection;
use crate::diesel::prelude::*;
use crate::feeds::models::*;
use crate::helpers::fetch_stories;
use crate::stories::models::*;

type DbPool = Pool<ConnectionManager<PgConnection>>;

#[derive(Debug, Deserialize)]
struct GetStoriesQueryString {
    refresh: Option<bool>,
}

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
    // execute sync code in threadpool
    web::block(move || get_stories(query, pool)).then(|res| match res {
        Ok(user) => Ok(HttpResponse::Ok().json(user)),
        Err(_) => Ok(HttpResponse::InternalServerError().into()),
    })
}

pub fn stories_config(cfg: &mut web::ServiceConfig) {
    cfg.service(web::resource("/v0/stories").route(web::get().to_async(get_stories_handler)));
}

// #[derive(Clone, Debug)]
// pub struct StoriesResource;
//
// #[derive(Response)]
// struct StoriesResponse {
//     stories: Vec<Story>,
// }
//
// #[derive(Response)]
// struct StoryResponse(Story);
//
// #[derive(Extract)]
// struct GetStoriesQueryString {
//     refresh: Option<bool>,
// }
//
// #[derive(Extract)]
// struct PatchStoryBody {
//     #[serde(rename = "isRead")]
//     is_read: Option<bool>,
// }
//
// #[derive(Extract)]
// struct PatchStoriesBody(Vec<StoryUpdate>);
//
// impl_web! {
//     impl StoriesResource {
//
//         #[get("/v0/stories/:story_id")]
//         #[content_type("json")]
//         fn get_story(&self, story_id: i32,  Connection(conn): Connection) -> Result<StoryResponse, ()> {
//             use crate::schema::stories::dsl::*;
//
//             let story = stories
//                 .filter(id.eq(story_id))
//                 .first::<Story>(&conn)
//                 .expect("Error loading story");
//
//             Ok(StoryResponse(story))
//         }
//
//         #[get("/v0/stories")]
//         #[content_type("json")]
//         fn get_stories(&self, query_string: GetStoriesQueryString, Connection(conn): Connection) -> Result<StoriesResponse, ()> {
//             use crate::schema::stories::dsl::*;
//             use crate::schema::feeds::dsl::*;
//
//             if let Some(refresh) = query_string.refresh {
//                 if refresh {
//                     let results = feeds
//                         .load::<Feed>(&conn)
//                         .expect("Error loading feeds");
//
//                     let stories_list: Vec<NewStory> = results.iter()
//                         .flat_map(|feed| fetch_stories(&feed.url, feed.id).unwrap())
//                         .collect();
//
//                     insert_into(stories)
//                         .values(stories_list)
//                         .on_conflict_do_nothing()
//                         .execute(&conn).unwrap();
//                 }
//             }
//
//
//             let results = stories
//                 .filter(is_read.eq(false))
//                 .order(published_date.desc())
//                 .load::<Story>(&conn)
//                 .expect("Error loading stories");
//
//             Ok(StoriesResponse {
//                 stories: results
//             })
//         }
//
//         #[patch("/v0/stories/:story_id")]
//         #[content_type("json")]
//         fn patch_story(&self, story_id: i32, body: PatchStoryBody, Connection(conn): Connection) -> Result<StoryResponse, ()> {
//             let story = StoryUpdate {
//                 id: story_id,
//                 is_read: body.is_read
//             };
//
//             let updated_story = story.save_changes(&*conn);
//
//             Ok(StoryResponse(updated_story.unwrap()))
//         }
//
//         #[patch("/v0/stories")]
//         #[content_type("json")]
//         fn patch_stories(&self, body: PatchStoriesBody, Connection(conn): Connection) -> Result<StoriesResponse, ()> {
//             let PatchStoriesBody(stories) = body;
//
//             let updated_stories = stories.iter()
//                 .map(|item| item.save_changes(&*conn).unwrap())
//                 .collect();
//
//             Ok(StoriesResponse { stories: updated_stories})
//         }
//
//     }
// }
