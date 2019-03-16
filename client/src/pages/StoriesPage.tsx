import React, { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'

interface Story {
  isRead: boolean
  url: string
  title: string
  publishedDate: string
}

async function fetchStories(setStories: (stories: Story[]) => void) {
  const res = await fetch('/api/v0/stories')

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
      {stories && (
        <ul>
          {stories.map(story => (
            <li>
              <a href={story.url}>{story.title}</a> - <Link to={`/story?page=${story.url}`}>Reader mode</Link>
            </li>
          ))}
        </ul>
      )}
    </div>
  )
}
