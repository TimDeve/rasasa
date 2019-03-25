import React, { useState, useEffect } from 'react'
import queryString from 'query-string'
import { RouteComponentProps } from 'react-router-dom'

import s from './StoryPage.scss'
import Title from 'shared/components/Title'

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

  useEffect(
    () => {
      if (typeof page === 'string') {
        fetchStory(page, setStory)
      }
    },
    [page]
  )

  if (!story) {
    return null
  }

  if (!story.readable || !story.content) {
    return <>Sorry this page is not readable</>
  }

  return (
    <div className={s.component}>
      <Title>{story.title}</Title>
      <p style={{ fontWeight: 'bold' }}>{story.byline}</p>
      <div className={s.article} dangerouslySetInnerHTML={{ __html: story.content }} />
    </div>
  )
}

export default StoryPage
