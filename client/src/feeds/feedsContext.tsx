import React, { useState, useEffect, ReactNode } from 'react'

import db from './FeedsDb'
import { Feed, FeedList } from './feedsModel'

interface Feeds {
  [key: number]: Feed
}

interface FeedLists {
  [key: number]: FeedList
}

export const FeedsContext = React.createContext<{
  feeds: Feeds
  feedLists: FeedLists
  refreshListsAndFeeds: () => void
}>({
  feeds: {},
  feedLists: {},
  refreshListsAndFeeds: () => {},
})

export function FeedsProvider({ children }: { children?: ReactNode }) {
  const [feeds, setFeeds] = useState<Feeds>({})
  const [feedLists, setFeedLists] = useState<FeedLists>({})

  async function refreshLists() {
    try {
      const res = await fetch('/api/v0/lists')
      const json = await res.json()

      setFeedLists(json.lists.reduce((acc: FeedLists, list: FeedList) => ({ ...acc, [list.id]: list }), {}))

      try {
        await db.feedLists.clear()
        await db.feedLists.bulkAdd(json.lists)
      } catch (e) {
        console.error('Failed to cache lists', e)
      }
    } catch (e) {
      console.log('Network failed serving cached lists')
      const lists = await db.feedLists
        .orderBy('id')
        .reverse()
        .toArray()

      setFeedLists(lists.reduce((acc: FeedLists, list: FeedList) => ({ ...acc, [list.id]: list }), {}))
    }
  }

  async function refreshFeeds() {
    try {
      const res = await fetch('/api/v0/feeds')
      const json = await res.json()

      setFeeds(json.feeds.reduce((acc: Feeds, feed: Feed) => ({ ...acc, [feed.id]: feed }), {}))

      try {
        await db.feeds.clear()
        await db.feeds.bulkAdd(json.feeds)
      } catch (e) {
        console.error('Failed to cache feeds', e)
      }
    } catch (e) {
      console.log('Network failed serving cached feeds')
      const feeds = await db.feeds
        .orderBy('id')
        .reverse()
        .toArray()

      setFeeds(feeds.reduce((acc: Feeds, feed: Feed) => ({ ...acc, [feed.id]: feed }), {}))
    }
  }

  function refreshListsAndFeeds() {
    refreshLists()
    refreshFeeds()
  }

  useEffect(() => {
    refreshListsAndFeeds()
  }, [])

  return (
    <FeedsContext.Provider value={{ feeds, feedLists, refreshListsAndFeeds }}>
      {children}
    </FeedsContext.Provider>
  )
}
