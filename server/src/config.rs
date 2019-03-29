use diesel::r2d2::{ConnectionManager, Pool, PooledConnection};
use diesel::PgConnection;
use tower_web::extract::{Context, Extract, Immediate};
use tower_web::util::BufStream;

pub struct Config {
    pub pool: Pool<ConnectionManager<PgConnection>>,
}

pub struct Connection(pub PooledConnection<ConnectionManager<PgConnection>>);

impl<B: BufStream> Extract<B> for Connection {
    type Future = Immediate<Connection>;

    fn extract(context: &Context) -> Self::Future {
        let config = context.config::<Config>().unwrap();
        Immediate::ok(Connection(config.pool.get().unwrap()))
    }
}
