package messages

import "github.com/TimDeve/rasasa/rss/api"

type OpenListsPage struct{}
type ListsFetched struct{ Lists []api.List }
