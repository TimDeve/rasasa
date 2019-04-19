import React, { useEffect, useState } from 'react'
import { Link, RouteComponentProps } from 'react-router-dom'
import queryString from 'query-string'
import cn from 'classnames'

import Title from 'shared/components/Title'
import Button from 'shared/components/Button'
import { Story } from './storiesModel'
import StoryPage from './StoryPage'
import StoryListItem from './StoryListItem'
import s from './StoriesPage.scss'
import db from './StoriesDb'
import {
  StoriesDispatch,
  useStories,
  markStoryAsRead,
  markAllStoriesAsRead,
  setAllStories,
  setStoriesLoading,
  clearStories as clearStoriesAction,
} from './StoriesPageState'

async function fetchStories(dispatch: StoriesDispatch, options: { refresh?: boolean } = {}) {
  try {
    dispatch(setStoriesLoading(true))
    const res = await fetch('/api/v0/stories?' + queryString.stringify(options))

    const json = await res.json()

    dispatch(setAllStories(json.stories))
    dispatch(setStoriesLoading(false))

    try {
      await db.stories.clear()
      await db.stories.bulkAdd(json.stories)
    } catch (e) {
      console.error('Failed to cache stories', e)
    }
  } catch (e) {
    console.log('Network failed serving cached stories')
    const stories = await db.stories
      .orderBy('id')
      .reverse()
      .toArray()
    dispatch(setAllStories(stories))
    dispatch(setStoriesLoading(false))
  }
}

async function setStoryToRead(dispatch: StoriesDispatch, storyId: number) {
  try {
    await db.stories
      .where('id')
      .equals(storyId)
      .modify({ isRead: true })
  } catch (e) {
    console.error('Failed to cache read status of story', e)
  }

  dispatch(markStoryAsRead(storyId))

  const res = await fetch(`/api/v0/stories/${storyId}`, {
    method: 'PATCH',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({
      isRead: true,
    }),
  })
}

async function setStoriesToRead(stories: Story[], dispatch: StoriesDispatch) {
  try {
    await db.stories
      .where('id')
      .anyOf(stories.map(s => s.id))
      .modify({ isRead: true })
  } catch (e) {
    console.error('Failed to cache read status of stories', e)
  }

  dispatch(markAllStoriesAsRead())

  const res = await fetch(`/api/v0/stories`, {
    method: 'PATCH',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(stories.map(({ id }) => ({ id, isRead: true }))),
  })
}

async function clearStories(stories: Story[], dispatch: StoriesDispatch) {
  try {
    await db.stories
      .where('id')
      .anyOf(stories.map(s => s.id))
      .delete()
  } catch (e) {
    console.error('Failed to delete cached stories', e)
  }

  dispatch(clearStoriesAction())

  const res = await fetch(`/api/v0/stories`, {
    method: 'PATCH',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(stories.filter(({ isRead }) => !isRead).map(({ id }) => ({ id, isRead: true }))),
  })
}

async function cacheStoriesAndArticles(stories: Story[]) {
  for (const story of stories) {
    const fetchStory = async function() {
      const res = await fetch('/api/v0/read?' + queryString.stringify({ page: story.url }))

      const json = await res.json()

      try {
        await db.articles.add({ ...json, timestamp: new Date().getTime() })
      } catch (e) {
        console.error('Failed to cache article', e)
      }
    }

    fetchStory()
  }
}

export default function StoriesPage(props: RouteComponentProps<{ storyId: string }>) {
  const [{ stories, loading }, dispatch] = useStories()

  useEffect(() => {
    fetchStories(dispatch)
  }, [])

  return (
    <>
      {props.location.pathname.indexOf('/story/') !== -1 && <StoryPage {...props} />}
      <div className={s.component}>
        <Title onClick={() => fetchStories(dispatch, { refresh: true })}>Stories</Title>
        <div>
          <Button className={s.button} onClick={() => setStoriesToRead(stories, dispatch)}>
            Mark all as read
          </Button>
          <Button className={s.button} onClick={() => cacheStoriesAndArticles(stories)}>
            Cache all
          </Button>
        </div>
        {!loading &&
          (stories.length === 0 ? (
            <p className={s.noStoriesMessage}>There are no stories here. Try to refresh.</p>
          ) : (
            <>
              <ul className={s.stories}>
                {stories.map(story => (
                  <StoryListItem
                    {...story}
                    key={story.id}
                    markAsRead={() => setStoryToRead(dispatch, story.id)}
                  />
                ))}
              </ul>
              <Button className={s.bottomButton} onClick={() => clearStories(stories, dispatch)}>
                Clear stories
              </Button>
            </>
          ))}
      </div>
    </>
  )
}
