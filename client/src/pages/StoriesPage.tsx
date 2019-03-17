import React, { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import queryString from 'query-string'

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
    <div>
      <h1>Stories</h1>
      <button onClick={() => fetchStories(setStories, { refresh: true })}>Refresh</button>
      {stories && (
        <ul>
          {stories.map(story => (
            <li key={story.id}>
              <a href={story.url}>{story.title}</a> - <Link to={`/story?page=${story.url}`}>Reader mode</Link>
            </li>
          ))}
        </ul>
      )}
    </div>
  )
}
