import React, { useState, useEffect, ReactNode, forwardRef } from 'react'
import queryString from 'query-string'
import { Link } from 'react-router-dom'
import { RouteComponentProps } from 'react-router-dom'
import ScrollLock, { TouchScrollable } from 'react-scrolllock'
import posed from 'react-pose'

import s from './StoryPage.scss'
import Title from 'shared/components/Title'
import { Article, Story } from './storiesModel'
import db from './StoriesDb'

interface StoryPageProps extends RouteComponentProps<{ storyId: string }> {}

function fetchArticle(page: string): Article | null {
  const [article, setArticle] = useState<Article | null>(null)

  useEffect(
    () => {
      ;(async () => {
        if (page) {
          const cachedArticle = await db.articles.get(page)

          if (cachedArticle) {
            setArticle(cachedArticle)
          } else {
            const res = await fetch('/api/v0/read?' + queryString.stringify({ page }))

            const json = await res.json()

            setArticle(json)

            try {
              await db.articles.add({ ...json, timestamp: new Date().getTime() })
            } catch (e) {
              console.error('Failed to cache article', e)
            }
          }
        }
      })()
    },
    [page]
  )

  return article
}

function fetchStory(id: string): Story | null {
  const [story, setStory] = useState<Story | null>(null)

  useEffect(
    () => {
      ;(async () => {
        if (id) {
          const cachedStory = await db.stories.get(parseInt(id))

          if (cachedStory) {
            setStory(cachedStory)
          } else {
            const res = await fetch(`/api/v0/stories/${id}`)

            if (res.ok) {
              const json = await res.json()

              setStory(json)

              try {
                await db.stories.add(json)
              } catch (e) {
                console.error('Failed to cache story', e)
              }
            }
          }
        }
      })()
    },
    [id]
  )

  return story
}

function Wrapper({ children, Aniref }: { children?: ReactNode }) {
  return (
    <>
      <ScrollLock />
      <TouchScrollable>
        <div ref={Aniref} className={s.component}>
          <div className={s.wrapper}>
            <div className={s.nav}>
              <Link to="/">Back to stories</Link>
            </div>
            {children}
          </div>
        </div>
      </TouchScrollable>
    </>
  )
}

function StoryPage(props: StoryPageProps) {
  const id = props.match.params.storyId || ''

  const story = fetchStory(id)

  const article = fetchArticle(story ? story.url : '')

  if (!story || !article) {
    return <Wrapper Aniref={props.Aniref} />
  }

  if (!article.readable || !article.content) {
    return (
      <Wrapper Aniref={props.Aniref}>
        <Title>
          <a style={{ color: 'black', textDecoration: 'none' }} href={story.url}>
            {story.title}
          </a>
        </Title>
        <>Sorry this page is not readable</>
      </Wrapper>
    )
  }

  return (
    <Wrapper Aniref={props.Aniref}>
      <Title>
        <a style={{ color: 'black', textDecoration: 'none' }} href={story.url}>
          {article.title}
        </a>
      </Title>
      <p style={{ fontWeight: 'bold' }}>{article.byline}</p>
      <div className={s.article} dangerouslySetInnerHTML={{ __html: article.content }} />
    </Wrapper>
  )
}

const AnimatedStoryPage = forwardRef((props, ref) => <StoryPage Aniref={ref} {...props} />)

export default posed(AnimatedStoryPage)({
  enter: {
    x: 0,
    transition: {
      duration: 300,
      ease: 'easeIn',
    },
  },
  flip: {
    transition: {
      ease: 'easeIn',
    },
  },
  exit: {
    x: '100vw',
    transition: {
      duration: 300,
      ease: 'easeOut',
    },
  },
})
