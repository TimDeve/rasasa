use diesel::insert_into;
use diesel::r2d2::{ConnectionManager, Pool, PooledConnection};
use diesel::PgConnection;
use std::time::Duration;

use crate::diesel::prelude::*;
use crate::feeds::models::*;
use crate::helpers::fetch_stories;
use crate::stories::models::*;

type PgPool = Pool<ConnectionManager<PgConnection>>;
type PgPooledConnection = PooledConnection<ConnectionManager<PgConnection>>;

pub fn setup_scheduler(pool: PgPool) -> clokwerk::ScheduleHandle {
    use clokwerk::{Scheduler, TimeUnits};

    let mut scheduler = Scheduler::new();

    {
        let pool = Box::new(pool.clone());
        scheduler
            .every(1.day())
            .at("3:00 am")
            .run(move || match pool.get() {
                Ok(db_conn) => delete_old_stories(db_conn),
                Err(e) => error!("Failed to acquire db connection.\n{:?}", e),
            });
    }

    {
        let pool = Box::new(pool.clone());
        scheduler.every(3.minute()).run(move || match pool.get() {
            Ok(db_conn) => fetch_new_stories(db_conn),
            Err(e) => error!("Failed to acquire db connection.\n{:?}", e),
        });
    }

    scheduler.watch_thread(Duration::from_millis(1000))
}

pub fn run_startup_jobs(pool: PgPool) {
    match pool.get() {
        Ok(db_conn) => delete_old_stories(db_conn),
        Err(e) => error!("Failed to acquire db connection.\n{:?}", e),
    }
}

pub fn delete_old_stories(conn: PgPooledConnection) {
    use crate::schema::stories::dsl::*;
    use diesel::dsl::{now, IntervalDsl};

    let result = diesel::delete(stories.filter(created_at.lt(now - 7_i32.days()))).execute(&conn);

    match result {
        Ok(n) => info!("Number of old stories deleted: {}", n),
        Err(e) => error!("Failed to delete old stories.\n{:?}", e),
    }
}

pub fn fetch_new_stories(conn: PgPooledConnection) {
    use crate::schema::feeds::dsl::*;
    use crate::schema::stories::dsl::*;

    let results = feeds.load::<Feed>(&conn);

    match results {
        Err(e) => error!("Error loading feeds.\n{:?}", e),
        Ok(loaded_feeds) => {
            let stories_list: Vec<NewStory> = loaded_feeds
                .iter()
                .flat_map(|feed| fetch_stories(&feed.url, feed.id).unwrap_or(vec![]))
                .collect();

            let db_result = insert_into(stories)
                .values(stories_list)
                .on_conflict_do_nothing()
                .execute(&conn);

            match db_result {
                Ok(_) => info!("Fetched new stories"),
                Err(e) => error!("Failed to insert new stories in DB.\n{:?}", e),
            }
        }
    }
}
