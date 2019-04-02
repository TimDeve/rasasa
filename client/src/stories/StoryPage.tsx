import queryString from 'query-string'
import React, { useState, useEffect } from 'react'
import { RouteComponentProps } from 'react-router-dom'

import s from './StoryPage.scss'
import Title from 'shared/components/Title'
import { Article, Story } from './storiesModel'

interface StoryPageProps extends RouteComponentProps<{ storyId: string }> {}

function fetchArticle(page: String): Article | null {
  const [article, setArticle] = useState<Article | null>(null)

  useEffect(
    () => {
      ;(async () => {
        if (page) {
          const res = await fetch('/api/v0/read?' + queryString.stringify({ page }))

          const json = await res.json()

          setArticle(json)
        }
      })()
    },
    [page]
  )

  return article
}

function fetchStory(id: String): Story | null {
  const [story, setStory] = useState<Story | null>(null)

  useEffect(
    () => {
      ;(async () => {
        if (id) {
          const res = await fetch(`/api/v0/stories/${id}`)

          const json = await res.json()

          setStory(json)
        }
      })()
    },
    [id]
  )

  return story
}

function StoryPage(props: StoryPageProps) {
  const id = props.match.params.storyId || ''

  const story = fetchStory(id)

  const article = fetchArticle(story ? story.url : '')

  console.log(story)

  if (!story || !article) {
    return null
  }

  if (!article.readable || !article.content) {
    return (
      <div className={s.component}>
        <Title>
          <a style={{ color: 'black', textDecoration: 'none' }} href={story.url}>
            {story.title}
          </a>
        </Title>
        <>Sorry this page is not readable</>
      </div>
    )
  }

  return (
    <div className={s.component}>
      <Title>
        <a style={{ color: 'black', textDecoration: 'none' }} href={story.url}>
          {article.title}
        </a>
      </Title>
      <p style={{ fontWeight: 'bold' }}>{article.byline}</p>
      <div className={s.article} dangerouslySetInnerHTML={{ __html: article.content }} />
    </div>
  )
}

export default StoryPage
