import React, { useState, useEffect } from 'react'

import { getInputValuesFromFormEvent } from 'shared/helpers'

interface Feed {
  id: number
  name: string
  url: string
}

async function fetchFeeds(setFeeds: (feeds: Feed[]) => void) {
  const res = await fetch('/api/v0/feeds')

  const json = await res.json()

  setFeeds(json.feeds)
}

async function removeFeed(feedId: number, feeds: Feed[], setFeeds: (feeds: Feed[]) => void) {
  const res = await fetch(`/api/v0/feeds/${feedId}`, {
    method: 'DELETE',
  })

  setFeeds(feeds.filter(f => f.id !== feedId))
}

async function addFeed(newFeed: object, callback: () => void) {
  const res = await fetch(`/api/v0/feeds`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(newFeed),
  })

  callback()
}

function FeedsPage() {
  const [feeds, setFeeds] = useState<Feed[] | null>(null)

  useEffect(() => {
    fetchFeeds(setFeeds)
  }, [])

  return (
    <div>
      <h1>Feeds</h1>

      <div>
        <span>Add a feed:</span>
        <form
          onSubmit={(e: React.ChangeEvent<HTMLFormElement>) => {
            const inputs = getInputValuesFromFormEvent(e)
            addFeed(inputs, () => fetchFeeds(setFeeds))
          }}
        >
          <input type="text" name="name" placeholder="Name" />
          <br />
          <input type="text" name="url" placeholder="Url" />
          <br />
          <button>Add</button>
        </form>
      </div>

      {feeds && (
        <ul>
          {feeds.map(feed => (
            <li key={feed.id}>
              Name: {feed.name}
              <br />
              Url: <a href={feed.url}>{feed.url}</a>
              <br />
              <button onClick={() => removeFeed(feed.id, feeds, setFeeds)}>Remove</button>
            </li>
          ))}
        </ul>
      )}
    </div>
  )
}

export default FeedsPage
