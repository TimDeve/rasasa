import React, { useState, useRef, useEffect, useCallback } from 'react'
import cn from 'classnames'
import { Link } from 'react-router-dom'

import s from './StoryListItem.scss'
import BoxArrow from 'shared/icons/BoxArrow'
import Chevron from 'shared/icons/Chevron'
import useToggle from 'shared/useToggle'
import { useElementHasExitedTopScreen } from 'shared/intersectionHooks'

interface StoryListItemProps {
  id: number | string
  url: string
  title: string
  content: string
  isRead: boolean
  markAsRead: () => void
}

function StoryListItem({ id, url, title, isRead, content, markAsRead }: StoryListItemProps) {
  const [hasContent, toggleContent] = useToggle(false)

  const ref = useElementHasExitedTopScreen(() => {
    if (!isRead) {
      markAsRead()
    }
  })

  return (
    <li className={s.component} ref={ref}>
      <div className={s.titleContainer}>
        <Link to={`/story/${id}`} className={cn(s.link, { [s.linkRead]: isRead })} onClick={markAsRead}>
          {title}
        </Link>
        <div className={s.actions}>
          {content && (
            <Chevron
              direction={hasContent ? 'up' : 'down'}
              onClick={toggleContent}
              color={isRead ? 'grey' : 'black'}
            />
          )}
          <a href={url} target="_blank" rel="noopener noreferrer" onClick={markAsRead}>
            <BoxArrow color={isRead ? 'grey' : 'black'} />
          </a>
        </div>
      </div>
      {hasContent && <div className={s.storyContent} dangerouslySetInnerHTML={{ __html: content }} />}
    </li>
  )
}

export default StoryListItem
