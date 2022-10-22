use std::time::Duration;

use crate::stories::services::{delete_old_stories, fetch_new_stories};
use crate::PgPool;

pub fn setup_scheduler(pool: PgPool) -> clokwerk::ScheduleHandle {
    use clokwerk::{Scheduler, TimeUnits};

    run_startup_jobs(pool.clone());

    let mut scheduler = Scheduler::new();

    {
        let pool = Box::new(pool.clone());
        scheduler
            .every(1.day())
            .at("3:00 am")
            .run(move || match pool.get() {
                Ok(db_conn) => delete_old_stories(&db_conn),
                Err(e) => error!("Failed to acquire db connection.\n{:?}", e),
            });
    }

    {
        let pool = Box::new(pool.clone());
        scheduler.every(3.minute()).run(move || match pool.get() {
            Ok(db_conn) => fetch_new_stories(&db_conn)
                .map(|_| info!("Fetched new stories"))
                .expect("Could not fetch new stories"),
            Err(e) => error!("Failed to acquire db connection.\n{:?}", e),
        });
    }

    scheduler.watch_thread(Duration::from_millis(1000))
}

fn run_startup_jobs(pool: PgPool) {
    match pool.get() {
        Ok(db_conn) => delete_old_stories(&db_conn),
        Err(e) => error!("Failed to acquire db connection.\n{:?}", e),
    }
}
