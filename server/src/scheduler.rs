use diesel::r2d2::{ConnectionManager, Pool};
use diesel::PgConnection;
use std::time::Duration;

use crate::diesel::prelude::*;

type PgPool = Pool<ConnectionManager<PgConnection>>;

pub fn setup_scheduler(pool: PgPool) -> clokwerk::ScheduleHandle {
    use clokwerk::{Scheduler, TimeUnits};

    let mut scheduler = Scheduler::new();

    {
        let pool = Box::new(pool.clone());
        scheduler
            .every(1.day())
            .at("3:00 am")
            .run(move || delete_old_stories(&pool));
    }

    scheduler.watch_thread(Duration::from_millis(1000))
}

pub fn delete_old_stories(pool: &PgPool) {
    use crate::schema::stories::dsl::*;
    use diesel::dsl::{now, IntervalDsl};

    let conn = pool.get().unwrap();

    let result = diesel::delete(stories.filter(created_at.lt(now - 30_i32.days()))).execute(&conn);

    match result {
        Ok(n) => info!("Number of old stories deleted: {}", n),
        Err(e) => error!("Failed to delete old stories.\n{:?}", e),
    }
}
