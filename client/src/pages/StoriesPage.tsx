import React, { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import queryString from 'query-string'

import Title from 'shared/components/Title'
import Button from 'shared/components/Button'
import s from './StoriesPage.scss'

interface Story {
  id: number
  isRead: boolean
  url: string
  title: string
  publishedDate: string
}

async function fetchStories(setStories: (stories: Story[]) => void, options: { refresh?: boolean } = {}) {
  const res = await fetch('/api/v0/stories?' + queryString.stringify(options))

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
      <Button onClick={() => fetchStories(setStories, { refresh: true })}>Refresh</Button>
      {stories && (
        <ul className={s.list}>
          {stories.map(story => (
            <>
              <li className={s.story} key={story.id}>
                <div>
                <a className={s.link} href={story.url}>
                  {story.title}
                </a>
              </div>
                <div className={s.actions}>
                  <Link to={`/story?page=${story.url}`}>Read</Link>
                </div>
              </li>
            </>
          ))}
        </ul>
      )}
    </div>
  )
}
