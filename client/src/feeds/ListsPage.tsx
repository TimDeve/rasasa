import React, { useContext } from 'react'
import { Link, RouteComponentProps } from 'react-router-dom'

import Title from 'shared/components/Title'
import Button from 'shared/components/Button'
import { FeedsContext } from './feedsContext'

import s from './ListsPage.scss'

function ListsPage() {
  const { feedLists, refreshListsAndFeeds } = useContext(FeedsContext)

  const lists = Object.values(feedLists)

  return (
    <div className={s.component}>
      <Title>Feed Lists</Title>

      {lists && (
        <ul className={s.lists}>
          <li key={'all'} className={s.list}>
            <Link to={`/`}>
              <h2>All stories</h2>
            </Link>
          </li>
          {lists.map(list => (
            <li key={list.id} className={s.list}>
              <Link to={`/lists/${list.id}`}>
                <h2>{list.name}</h2>
              </Link>
            </li>
          ))}
        </ul>
      )}
    </div>
  )
}

export default ListsPage
