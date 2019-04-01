import queryString from 'query-string'
import React, { useState, useEffect } from 'react'
import { RouteComponentProps } from 'react-router-dom'

import s from './StoryPage.scss'
import Title from 'shared/components/Title'

interface Read {
  readable: boolean
  title?: String
  byline?: String
  content?: string
}

interface StoryPageProps extends RouteComponentProps<{}> {}

function fetchRead(page: String): Read | null {
  const [story, setStory] = useState<Read | null>(null)

  useEffect(
    () => {
      ;(async () => {
        if (page) {
          const res = await fetch('/api/v0/read?' + queryString.stringify({ page }))

          const json = await res.json()

          setStory(json)
        }
      })()
    },
    [page]
  )

  return story
}

function StoryPage({ history }: StoryPageProps) {
  const { page } = queryString.parse(history.location.search)

  let read = fetchRead(typeof page === 'string' ? page : '')

  if (!read) {
    return null
  }

  if (!read.readable || !read.content) {
    return <>Sorry this page is not readable</>
  }

  return (
    <div className={s.component}>
      <Title>{read.title}</Title>
      <p style={{ fontWeight: 'bold' }}>{read.byline}</p>
      <div className={s.article} dangerouslySetInnerHTML={{ __html: read.content }} />
    </div>
  )
}

export default StoryPage
