#[macro_use]
extern crate tower_web;
extern crate tokio;

use tower_web::ServiceBuilder;

#[derive(Clone, Debug)]
struct App;

#[derive(Response)]
struct NameResponse {
    name: String,
}

impl_web! {
    impl App {
        #[get("/v0/name/:name")]
        #[content_type("json")]
        fn name_endpoint(&self, name: String) -> Result<NameResponse, ()> {
            Ok(NameResponse {
                name,
            })
        }
    }
}

pub fn main() {
    let addr = "127.0.0.1:8091".parse().expect("Invalid address");
    println!("Listening on http://{}", addr);

    ServiceBuilder::new().resource(App).run(&addr).unwrap();
}
