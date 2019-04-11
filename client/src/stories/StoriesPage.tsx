import React, { useEffect, useState } from 'react'
import { Link, RouteComponentProps } from 'react-router-dom'
import queryString from 'query-string'
import cn from 'classnames'

import Title from 'shared/components/Title'
import Button from 'shared/components/Button'
import { Story } from './storiesModel'
import StoryPage from './StoryPage'
import s from './StoriesPage.scss'
import db from './StoriesDb'

async function fetchStories(setStories: (stories: Story[]) => void, options: { refresh?: boolean } = {}) {
  try {
    const res = await fetch('/api/v0/stories?' + queryString.stringify(options))

    const json = await res.json()

    setStories(json.stories)

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
    setStories(stories)
  }
}

async function setStoryToRead(stories: Story[], setStories: (stories: Story[]) => void, storyId: number) {
  try {
    await db.stories
      .where('id')
      .equals(storyId)
      .modify({ isRead: true })
  } catch (e) {
    console.error('Failed to cache read status of story', e)
  }

  setStories(stories.map(story => (story.id === storyId ? { ...story, isRead: true } : story)))

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

async function setStoriesToRead(stories: Story[], setStories: (stories: Story[]) => void) {
  try {
    await db.stories
      .where('id')
      .anyOf(stories.map(s => s.id))
      .modify({ isRead: true })
  } catch (e) {
    console.error('Failed to cache read status of stories', e)
  }

  setStories(stories.map(s => ({ ...s, isRead: true })))

  const res = await fetch(`/api/v0/stories`, {
    method: 'PATCH',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(stories.map(({ id }) => ({ id, isRead: true }))),
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
  const [stories, setStories] = useState<Story[] | null>(null)

  useEffect(() => {
    fetchStories(setStories)
  }, [])

  return (
    <>
      {props.location.pathname.indexOf('/story/') !== -1 && <StoryPage {...props} />}
      <div className={s.component}>
        <Title>Stories</Title>
        <div>
          <Button className={s.button} onClick={() => fetchStories(setStories, { refresh: true })}>
            Refresh
          </Button>
          <Button className={s.button} onClick={() => setStoriesToRead(stories || [], setStories)}>
            Mark all as read
          </Button>
          <Button className={s.button} onClick={() => cacheStoriesAndArticles(stories || [])}>
            Cache all
          </Button>
        </div>
        {stories && (
          <>
            {stories.length === 0 && (
              <p className={s.noStoriesMessage}>There are no stories here. Try to refresh.</p>
            )}
            <ul className={s.list}>
              {stories.map(story => (
                <li className={s.story} key={story.id}>
                  <div>
                    <a
                      className={cn(s.link, { [s.linkRead]: story.isRead })}
                      target="_blank"
                      href={story.url}
                      onClick={() => setStoryToRead(stories, setStories, story.id)}
                    >
                      {story.title}
                    </a>
                  </div>
                  <div className={s.actions}>
                    <Link
                      to={`/story/${story.id}`}
                      onClick={() => setStoryToRead(stories, setStories, story.id)}
                    >
                      Read
                    </Link>
                  </div>
                </li>
              ))}
            </ul>
          </>
        )}
      </div>
    </>
  )
}
