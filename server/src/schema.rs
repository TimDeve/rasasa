table! {
    feed_lists (id) {
        id -> Int4,
        feed_id -> Int4,
        list_id -> Int4,
    }
}

table! {
    feeds (id) {
        id -> Int4,
        name -> Text,
        url -> Text,
    }
}

table! {
    lists (id) {
        id -> Int4,
        name -> Text,
    }
}

table! {
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

joinable!(feed_lists -> feeds (feed_id));
joinable!(feed_lists -> lists (list_id));
joinable!(stories -> feeds (feed_id));

allow_tables_to_appear_in_same_query!(feed_lists, feeds, lists, stories,);
