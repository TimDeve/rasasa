import db from './StoriesDb'
import queryString from 'query-string'
import uuid from 'uuid/v4'

import { Story } from './storiesModel'
import {
  StoriesDispatch,
  useStories,
  markStoryAsRead,
  markAllStoriesAsRead,
  setAllStories,
  setStoriesLoading,
  clearStories as clearStoriesAction,
} from './StoriesPageState'

export async function fetchStories(dispatch: StoriesDispatch, options: { refresh?: boolean } = {}) {
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

export async function setStoryToRead(dispatch: StoriesDispatch, storyId: number) {
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

export async function setStoriesToRead(stories: Story[], dispatch: StoriesDispatch) {
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

export async function clearStories(stories: Story[], dispatch: StoriesDispatch) {
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

export async function cacheStoriesAndArticles(stories: Story[]) {
  if ('BackgroundFetchManager' in self) {
    navigator.serviceWorker.ready.then(async (swReg: any) => {
      // Uses 'any' because backgroundFetch doesn't have a typedef yet
      try {
        const urls = stories.map(story => '/api/v0/read?' + queryString.stringify({ page: story.url }))
        await swReg.backgroundFetch.fetch('articles-fetch:' + uuid(), urls, {
          title: 'Fetching articles',
        })
      } catch (e) {
        console.log(e)
      }
    })
  } else {
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
}
