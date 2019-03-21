extern crate chrono;
extern crate diesel;
extern crate http;
extern crate rss;
extern crate tokio;

pub mod models;

use diesel::insert_into;

use crate::diesel::prelude::*;
use crate::feeds::models::*;
use crate::helpers::{establish_db_connection, fetch_stories};
use models::*;

#[derive(Clone, Debug)]
pub struct StoriesResource;

#[derive(Response)]
struct StoriesResponse {
    stories: Vec<Story>,
}

#[derive(Response)]
struct StoryResponse(Story);

#[derive(Extract)]
struct GetStoriesQueryString {
    refresh: Option<bool>,
}

#[derive(Extract)]
struct PatchStoryBody {
    #[serde(rename = "isRead")]
    is_read: Option<bool>
}

#[derive(Extract)]
struct PatchStoriesBody(Vec<StoryUpdate>);

impl_web! {
    impl StoriesResource {

        #[get("/v0/stories")]
        #[content_type("json")]
        fn get_stories(&self, query_string: GetStoriesQueryString) -> Result<StoriesResponse, ()> {
            use crate::schema::stories::dsl::*;
            use crate::schema::feeds::dsl::*;

            let connection = establish_db_connection();

            if let Some(refresh) = query_string.refresh {
                if refresh {
                    let results = feeds
                        .load::<Feed>(&connection)
                        .expect("Error loading feeds");

                    let stories_list: Vec<NewStory> = results.iter()
                        .flat_map(|feed| fetch_stories(&feed.url, feed.id).unwrap())
                        .collect();

                    insert_into(stories)
                        .values(stories_list)
                        .on_conflict_do_nothing()
                        .execute(&connection).unwrap();
                }
            }


            let results = stories
                .order(published_date.desc())
                .load::<Story>(&connection)
                .expect("Error loading stories");

            Ok(StoriesResponse {
                stories: results
            })
        }

        #[patch("/v0/stories/:story_id")]
        #[content_type("json")]
        fn patch_story(&self, story_id: i32, body: PatchStoryBody) -> Result<StoryResponse, ()> {
            let connection = establish_db_connection();

            let story = StoryUpdate {
                id: story_id,
                is_read: body.is_read
            };

            let updated_story = story.save_changes(&connection);

            Ok(StoryResponse(updated_story.unwrap()))
        }

        #[patch("/v0/stories")]
        #[content_type("json")]
        fn patch_stories(&self, body: PatchStoriesBody) -> Result<StoriesResponse, ()> {
            let connection = establish_db_connection();

            let PatchStoriesBody(stories) = body;

            let updated_stories = stories.iter()
                .map(|item| item.save_changes(&connection).unwrap())
                .collect();

            Ok(StoriesResponse { stories: updated_stories})
        }

    }
}
