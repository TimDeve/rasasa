import React, { useState, useEffect, useContext } from 'react'
import { Helmet } from 'react-helmet'

import { getInputValuesFromFormEvent, resetForm } from 'shared/helpers'
import { Feed } from './feedsModel'
import { FeedsContext } from './feedsContext'
import Title from 'shared/components/Title'
import TextInput from 'shared/components/TextInput'
import Button from 'shared/components/Button'
import s from './FeedsPage.scss'

async function removeFeed(feedId: number, feeds: Feed[], callback: () => void) {
  const res = await fetch(`/api/v0/feeds/${feedId}`, {
    method: 'DELETE',
  })

  callback()
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
  const { feeds: feedsMap, refreshListsAndFeeds } = useContext(FeedsContext)

  const feeds = Object.values(feedsMap)

  return (
    <div className={s.component}>
      <Helmet>
        <title>Rasasa - Feeds</title>
      </Helmet>

      <Title>Feeds</Title>

      <div>
        <span>Add a feed:</span>
        <form
          onSubmit={(e: React.ChangeEvent<HTMLFormElement>) => {
            const inputs = getInputValuesFromFormEvent(e)
            resetForm(e)
            addFeed(inputs, refreshListsAndFeeds)
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
                <Button onClick={() => removeFeed(feed.id, feeds, refreshListsAndFeeds)}>Remove</Button>
              </div>
            </li>
          ))}
        </ul>
      )}
    </div>
  )
}

export default FeedsPage
