import React, { useContext, useState } from 'react'
import { Link, RouteComponentProps } from 'react-router-dom'

import Title from 'shared/components/Title'
import Button from 'shared/components/Button'
import TextInput from 'shared/components/TextInput'
import { FeedsContext } from './feedsContext'
import { getInputValuesFromFormEvent, resetForm } from 'shared/helpers'

import s from './ListsPage.scss'

function ListsPage() {
  const { feeds, feedLists, refreshListsAndFeeds } = useContext(FeedsContext)
  const [inEditMode, setInEditMode] = useState(false)

  const lists = Object.values(feedLists)

  async function removeList(listId: number) {
    const res = await fetch(`/api/v0/lists/${listId}`, {
      method: 'DELETE',
    })

    refreshListsAndFeeds()
  }

  async function addList(name: string) {
    const res = await fetch(`/api/v0/lists`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        name,
      }),
    })

    refreshListsAndFeeds()
  }

  async function addFeed(feedId: number, listId: number) {
    const res = await fetch(`/api/v0/lists/${listId}/feed`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        feedId,
      }),
    })

    refreshListsAndFeeds()
  }

  async function removeFeed(feedId: number, listId: number) {
    const res = await fetch(`/api/v0/lists/${listId}/feed/${feedId}`, {
      method: 'DELETE',
    })

    refreshListsAndFeeds()
  }

  return (
    <div className={s.component}>
      <Title>Feed Lists</Title>

      <Button className={s.button} onClick={() => setInEditMode(!inEditMode)}>
        {inEditMode ? 'Stop editing' : 'Edit'}
      </Button>

      {inEditMode && (
        <div>
          <h3>Add a list:</h3>
          <form
            onSubmit={(e: React.ChangeEvent<HTMLFormElement>) => {
              const inputs = getInputValuesFromFormEvent(e)
              resetForm(e)
              addList(inputs.name)
            }}
          >
            <TextInput name="name" placeholder="Name" />
            <br />
            <Button>Add</Button>
          </form>
        </div>
      )}

      {lists && (
        <ul className={s.lists}>
          <li key={'all'} className={s.list}>
            <Link to={`/`}>
              <h2>All stories</h2>
            </Link>
          </li>
          {lists.map(list => (
            <li key={list.id} className={s.list}>
              <Link to={`/lists/${list.id}/${encodeURI(list.name.replace(/\s/g, "-"))}`}>
                <h2>{list.name}</h2>
              </Link>
              {inEditMode && (
                <>
                  <Button className={s.button} onClick={() => removeList(list.id)}>
                    Remove
                  </Button>
                  <ul>
                    <h3>Feeds in list:</h3>
                    {Object.values(feeds)
                      .filter(feed => list.feedIds.includes(feed.id))
                      .map(feed => (
                        <li key={feed.id}>
                          {feed.name}
                          <br />
                          <Button className={s.button} onClick={() => removeFeed(feed.id, list.id)}>
                            Remove
                          </Button>
                        </li>
                      ))}
                  </ul>
                  <ul>
                    <h3>Feeds not in list:</h3>
                    {Object.values(feeds)
                      .filter(feed => !list.feedIds.includes(feed.id))
                      .map(feed => (
                        <li key={feed.id}>
                          {feed.name}
                          <br />
                          <Button className={s.button} onClick={() => addFeed(feed.id, list.id)}>
                            Add
                          </Button>
                        </li>
                      ))}
                  </ul>
                </>
              )}
            </li>
          ))}
        </ul>
      )}
    </div>
  )
}

export default ListsPage
