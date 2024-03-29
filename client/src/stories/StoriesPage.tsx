import React, { useEffect, useContext, useState } from 'react'
import { useLocation, useParams, useSearchParams } from 'react-router-dom'
import { Helmet } from 'react-helmet'

import Title from 'shared/components/Title'
import Button from 'shared/components/Button'
import StoryPage from './StoryPage'
import StoryListItem from './StoryListItem'
import s from './StoriesPage.scss'
import {
  fetchStories,
  setStoriesToRead,
  cacheStoriesAndArticles,
  setStoryToRead,
  clearStories,
} from './StoriesPageGateway'
import { useStories } from './StoriesPageState'
import { FeedsContext } from 'feeds/feedsContext'
import { StoriesDispatch } from './StoriesPageState'

const nonBreakinSpaceChar = '\u00a0'

// Ugh this is ugly...
function useFetchStories(dispatch: StoriesDispatch, showReadStories: boolean, isStoryPage: boolean) {
  const [showRead, setShowRead] = useState(showReadStories)
  const params = useParams()

  const listId = params?.listId

  useEffect(() => {
    if (!isStoryPage) { setShowRead(showReadStories) }
  }, [showReadStories, isStoryPage])

  useEffect(() => {
    fetchStories(dispatch, { listId, read: showReadStories })
  }, [showRead])
}

export default function StoriesPage() {
  const [{ stories, loading }, dispatch] = useStories()
  const [searchParams, setSearchParams] = useSearchParams()
  const { feedLists } = useContext(FeedsContext)
  const location = useLocation()
  const params = useParams()

  const listId = params?.listId
  const title = listId ? feedLists?.[parseInt(listId)]?.name ?? nonBreakinSpaceChar : 'All stories'

  let showReadStories: boolean = searchParams.has('read')

  const isStoryPage = location.pathname.indexOf('/stories/') !== -1

  useFetchStories(dispatch, showReadStories, isStoryPage)

  return (
    <>
      <Helmet>
        <title>Rasasa - {title}</title>
      </Helmet>
      {isStoryPage && <StoryPage />}
      <div className={s.component}>
        <Title onClick={() => fetchStories(dispatch, { refresh: true, listId })}>{title}</Title>
        <div>
          <Button className={s.button} onClick={() => setStoriesToRead(stories, dispatch)}>
            Mark all as read
          </Button>
          <Button className={s.button} onClick={() => cacheStoriesAndArticles(stories)}>
            Cache all
          </Button>
          <Button
            className={s.button}
            onClick={() => (showReadStories ? setSearchParams({}) : setSearchParams({ read: 'true' }))}
          >
            {showReadStories ? 'Unread' : 'Read'}
          </Button>
        </div>
        {!loading &&
          (stories.length === 0 ? (
            <p className={s.noStoriesMessage}>There are no stories here. Try to refresh.</p>
          ) : (
            <>
              <ul className={s.stories}>
                {stories.map(story => (
                  <StoryListItem
                    {...story}
                    key={story.id}
                    markAsRead={() => setStoryToRead(dispatch, story.id)}
                  />
                ))}
              </ul>
              <Button className={s.bottomButton} onClick={() => clearStories(stories, dispatch)}>
                Clear stories
              </Button>
            </>
          ))}
      </div>
    </>
  )
}
