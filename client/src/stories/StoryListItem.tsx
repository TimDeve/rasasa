import React from 'react'
import cn from 'classnames'
import { Link } from 'react-router-dom'

import s from './StoryListItem.scss'
import BoxArrow from 'shared/icons/BoxArrow'

interface StoryListItemProps {
  id: number | string
  url: string
  title: string
  isRead: boolean
  markAsRead: () => void
}

function StoryListItem({ id, url, title, isRead, markAsRead }: StoryListItemProps) {
  return (
    <li className={s.component}>
      <Link to={`/story/${id}`} className={cn(s.link, { [s.linkRead]: isRead })}  onClick={markAsRead}>
        {title}
      </Link>
      <div className={s.actions}>
        <a href={url} target="_blank" rel="noopener noreferrer" onClick={markAsRead}>
          <BoxArrow color={isRead ? "grey" : "black"}/>
        </a>
      </div>
    </li>
  )
}

export default StoryListItem
