import React, { useState, useEffect } from 'react'
import queryString from 'query-string'
import { RouteComponentProps } from 'react-router-dom'

interface Story {
  readable: boolean
  title?: String
  byline?: String
  content?: string
}

interface StoryPageProps extends RouteComponentProps<{}> {}

async function fetchStory(page: String, setStory: (story: Story) => void) {
  const res = await fetch('/api/v0/read?' + queryString.stringify({ page }))

  const json = await res.json()

  setStory(json)
}

function StoryPage({ history }: StoryPageProps) {
  const { page } = queryString.parse(history.location.search)

  const [story, setStory] = useState<Story | null>(null)

  useEffect(() => {
    if (typeof page === 'string') {
      fetchStory(page, setStory)
    }
  }, [])

  if (!story) {
    return null
  }

  if (!story.readable || !story.content) {
    return <>Sorry this page is not readable</>
  }

  return (
    <>
      <h1>{story.title}</h1>
      <p style={{ fontWeight: 'bold' }}>{story.byline}</p>
      <div dangerouslySetInnerHTML={{ __html: story.content }} />
    </>
  )
}

export default StoryPage
