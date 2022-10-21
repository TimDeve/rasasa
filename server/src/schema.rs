// @generated automatically by Diesel CLI.

diesel::table! {
    feed_lists (id) {
        id -> Int4,
        feed_id -> Int4,
        list_id -> Int4,
    }
}

diesel::table! {
    feeds (id) {
        id -> Int4,
        name -> Text,
        url -> Text,
    }
}

diesel::table! {
    lists (id) {
        id -> Int4,
        name -> Text,
    }
}

diesel::table! {
    stories (id) {
        id -> Int4,
        feed_id -> Int4,
        title -> Text,
        url -> Text,
        is_read -> Bool,
        published_date -> Timestamptz,
        created_at -> Timestamp,
        content -> Text,
    }
}

diesel::joinable!(feed_lists -> feeds (feed_id));
diesel::joinable!(feed_lists -> lists (list_id));
diesel::joinable!(stories -> feeds (feed_id));

diesel::allow_tables_to_appear_in_same_query!(
    feed_lists,
    feeds,
    lists,
    stories,
);
