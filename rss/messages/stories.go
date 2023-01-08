package messages

import "github.com/TimDeve/rasasa/rss/api"

type OpenStoriesPage struct{ List *api.List }
type BackToStoriesPage struct{}
type StoriesFetched []api.Story
