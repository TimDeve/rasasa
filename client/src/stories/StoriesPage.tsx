import React, { useEffect, useState } from 'react'
import { Link, RouteComponentProps } from 'react-router-dom'
import cn from 'classnames'

import Title from 'shared/components/Title'
import Button from 'shared/components/Button'
import { Story } from './storiesModel'
import StoryPage from './StoryPage'
import StoryListItem from './StoryListItem'
import s from './StoriesPage.scss'
import db from './StoriesDb'
import {
  fetchStories,
  setStoriesToRead,
  cacheStoriesAndArticles,
  setStoryToRead,
  clearStories,
} from './StoriesPageGateway'
import { useStories } from './StoriesPageState'

export default function StoriesPage(props: RouteComponentProps<{ storyId: string }>) {
  const [{ stories, loading }, dispatch] = useStories()

  useEffect(() => {
    fetchStories(dispatch)
  }, [])

  return (
    <>
      {props.location.pathname.indexOf('/story/') !== -1 && <StoryPage {...props} />}
      <div className={s.component}>
        <Title onClick={() => fetchStories(dispatch, { refresh: true })}>Stories</Title>
        <div>
          <Button className={s.button} onClick={() => setStoriesToRead(stories, dispatch)}>
            Mark all as read
          </Button>
          <Button className={s.button} onClick={() => cacheStoriesAndArticles(stories)}>
            Cache all
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
