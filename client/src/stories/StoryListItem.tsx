import React, { useState, useRef, useEffect, useCallback, useContext } from 'react'
import cn from 'classnames'
import { Link } from 'react-router-dom'
import sanitizeHtml from 'sanitize-html'

import { Feed } from '../feeds/feedsModel'
import s from './StoryListItem.scss'
import BoxArrow from 'shared/icons/BoxArrow'
import Chevron from 'shared/icons/Chevron'
import useToggle from 'shared/useToggle'
import { useElementHasExitedTopScreen } from 'shared/intersectionHooks'
import { FeedsContext } from 'feeds/feedsContext'

interface StoryListItemProps {
  id: number | string
  url: string
  title: string
  content: string
  isRead: boolean
  feedId: number
  feed?: Feed
  markAsRead: () => void
}

function EmptyIcon() {
  return <span style={{ width: '24px' }} />
}

function StoryListItem({ id, url, title, isRead, content, markAsRead, feedId }: StoryListItemProps) {
  const { feeds } = useContext(FeedsContext)
  const [hasContent, toggleContent] = useToggle(false)

  const ref = useElementHasExitedTopScreen(() => {
    if (!isRead) {
      markAsRead()
    }
  })

  return (
    <li className={s.component} ref={ref}>
      <div className={s.titleContainer}>
        <Link to={`/stories/${id}`} className={cn(s.link, { [s.linkRead]: isRead })}>
          <span className={s.feedTitle}>{feeds[feedId] && feeds[feedId].name}</span>
          <span>{title}</span>
        </Link>
        <div className={s.actions}>
          {content ? (
            <Chevron
              direction={hasContent ? 'up' : 'down'}
              onClick={toggleContent}
              color={isRead ? 'grey' : 'black'}
            />
          ) : (
            <Chevron
              direction='flat'
              className={s.disabled}
              color={isRead ? 'grey' : 'black'}
            />
          )}
          <a href={url} target="_blank" rel="noopener noreferrer">
            <BoxArrow color={isRead ? 'grey' : 'black'} />
          </a>
        </div>
      </div>
      {hasContent && (
        <div className={s.storyContent} dangerouslySetInnerHTML={{ __html: sanitizeHtml(content) }} />
      )}
    </li>
  )
}

export default StoryListItem
