import queryString from 'query-string'
import { v4 as uuid } from 'uuid'

import storiesDb from './StoriesDb'
import feedsDb from 'feeds/FeedsDb'
import { Story } from './storiesModel'
import {
  StoriesDispatch,
  markStoryAsRead,
  markAllStoriesAsRead,
  setAllStories,
  setStoriesLoading,
  clearStories as clearStoriesAction,
} from './StoriesPageState'

export async function fetchStories(
  dispatch: StoriesDispatch,
  options: { refresh?: boolean; listId?: string; read?: boolean } = {}
) {
  try {
    dispatch(setStoriesLoading(true))
    const res = await fetch('/api/v0/stories?' + queryString.stringify(options))

    const json = await res.json()

    dispatch(setAllStories(json.stories))
    dispatch(setStoriesLoading(false))

    try {
      await storiesDb.stories.clear()
      await storiesDb.stories.bulkAdd(json.stories)
    } catch (e) {
      console.error('Failed to cache stories', e)
    }
  } catch (e) {
    console.log('Network failed serving cached stories')

    const articleUrls: Set<string> = new Set()
    await storiesDb.articles.toCollection().eachUniqueKey(url => articleUrls.add(url as string))

    let query
    if (options.listId) {
      const list = await feedsDb.feedLists.where('id').equals(Number(options.listId)).first()

      query = storiesDb.stories.where('feedId').anyOf(list ? list.feedIds : [])
    } else {
      query = storiesDb.stories.orderBy('id').reverse()
    }

    let stories = await query
      .filter(s => options.read || !s.isRead)
      .and(story => articleUrls.has(story.url))
      .toArray()
    dispatch(setAllStories(stories))
    dispatch(setStoriesLoading(false))
  }
}

export async function setStoryToRead(dispatch: StoriesDispatch, storyId: number) {
  try {
    await storiesDb.stories.where('id').equals(storyId).modify({ isRead: true })
  } catch (e) {
    console.error('Failed to cache read status of story', e)
  }

  dispatch(markStoryAsRead(storyId))

  await fetch(`/api/v0/stories/${storyId}`, {
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
    await storiesDb.stories
      .where('id')
      .anyOf(stories.map(s => s.id))
      .modify({ isRead: true })
  } catch (e) {
    console.error('Failed to cache read status of stories', e)
  }

  dispatch(markAllStoriesAsRead())

  await fetch(`/api/v0/stories`, {
    method: 'PATCH',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(stories.map(({ id }) => ({ id, isRead: true }))),
  })
}

export async function clearStories(stories: Story[], dispatch: StoriesDispatch) {
  try {
    await storiesDb.stories
      .where('id')
      .anyOf(stories.map(s => s.id))
      .delete()
  } catch (e) {
    console.error('Failed to delete cached stories', e)
  }

  dispatch(clearStoriesAction())

  await fetch(`/api/v0/stories`, {
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
      const fetchStory = async function () {
        const res = await fetch('/api/v0/read?' + queryString.stringify({ page: story.url }))

        const json = await res.json()

        try {
          await storiesDb.articles.add({ ...json, timestamp: new Date().getTime() })
        } catch (e) {
          console.error('Failed to cache article', e)
        }
      }

      fetchStory()
    }
  }
}
