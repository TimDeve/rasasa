import React, { useState, useEffect } from 'react'

import { getInputValuesFromFormEvent } from 'shared/helpers'
import Title from 'shared/components/Title'
import TextInput from 'shared/components/TextInput'
import Button from 'shared/components/Button'
import s from './FeedsPage.scss'

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
    <div className={s.component}>
      <Title>Feeds</Title>

      <div>
        <span>Add a feed:</span>
        <form
          onSubmit={(e: React.ChangeEvent<HTMLFormElement>) => {
            const inputs = getInputValuesFromFormEvent(e)
            addFeed(inputs, () => fetchFeeds(setFeeds))
          }}
        >
          <TextInput name="name" placeholder="Name" />
          <br />
          <TextInput type="text" name="url" placeholder="Url" />
          <br />
          <Button>Add</Button>
        </form>
      </div>

      {feeds && (
        <ul className={s.feeds}>
          {feeds.map(feed => (
            <li key={feed.id} className={s.feed}>
              <div>{feed.name}</div>
              <div>
                <a href={feed.url}>{feed.url}</a>
              </div>
              <div className={s.feedButtons}>
                <Button onClick={() => removeFeed(feed.id, feeds, setFeeds)}>Remove</Button>
              </div>
            </li>
          ))}
        </ul>
      )}
    </div>
  )
}

export default FeedsPage
