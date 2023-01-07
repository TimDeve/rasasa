package messages

import "github.com/TimDeve/rasasa/rss/api"

type OpenStoryPage struct{ Story api.Story }

type LoadStory struct{ Story api.Story }

type ArticleFetched struct{ Article api.Article }
