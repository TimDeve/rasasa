import React, { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import queryString from 'query-string'
import cn from 'classnames'

import Title from 'shared/components/Title'
import Button from 'shared/components/Button'
import { Story } from './storiesModel'
import s from './StoriesPage.scss'

async function fetchStories(setStories: (stories: Story[]) => void, options: { refresh?: boolean } = {}) {
  const res = await fetch('/api/v0/stories?' + queryString.stringify(options))

  const json = await res.json()

  setStories(json.stories)
}

async function setStoryToRead(stories: Story[], setStories: (stories: Story[]) => void, storyId: number) {
  const res = await fetch(`/api/v0/stories/${storyId}`, {
    method: 'PATCH',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({
      isRead: true,
    }),
  })

  const json = await res.json()

  setStories(stories.map(story => (story.id === json.id ? json : story)))
}

async function setStoriesToRead(stories: Story[], setStories: (stories: Story[]) => void) {
  const res = await fetch(`/api/v0/stories`, {
    method: 'PATCH',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(stories.map(({ id }) => ({ id, isRead: true }))),
  })

  const json = await res.json()

  setStories(json.stories)
}

export default function StoriesPage() {
  const [stories, setStories] = useState<Story[] | null>(null)

  useEffect(() => {
    fetchStories(setStories)
  }, [])

  return (
    <div className={s.component}>
      <Title>Stories</Title>
      <div>
        <Button className={s.button} onClick={() => fetchStories(setStories, { refresh: true })}>
          Refresh
        </Button>
        <Button className={s.button} onClick={() => setStoriesToRead(stories || [], setStories)}>
          Mark all as read
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
  )
}
