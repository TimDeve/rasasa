table! {
    feeds (id) {
        id -> Int4,
        name -> Text,
        url -> Text,
    }
}

table! {
    stories (id) {
        id -> Int4,
        feed_id -> Int4,
        title -> Text,
        url -> Text,
        content -> Text,
        is_read -> Bool,
        published_date -> Timestamptz,
        created_at -> Timestamp,
    }
}

joinable!(stories -> feeds (feed_id));

allow_tables_to_appear_in_same_query!(feeds, stories,);
