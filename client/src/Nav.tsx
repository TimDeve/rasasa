import React, { useContext } from 'react'
import { Link } from 'react-router-dom'

import { LoggedInContext, LoggedInState } from 'shared/loggedInState/LoggedInContext'
import ListIcon from 'shared/icons/ListIcon'
import FeedIcon from 'shared/icons/FeedIcon'
import ConnectionIcon from 'shared/icons/ConnectionIcon'
import s from './Nav.scss'

function Nav() {
  const loggedInState = useContext(LoggedInContext)

  return (
    <nav className={s.nav}>
      <ul>
        <li>
          <Link to="/lists">
            <ListIcon /> <span>Lists</span>
          </Link>
        </li>
        <li>
          <Link to="/feeds">
            <FeedIcon /> <span>Feeds</span>
          </Link>
        </li>
        {loggedInState === LoggedInState.LoggedOut && (
          <li className={s.rightSideItem}>
            <Link to="/login">
              <ConnectionIcon /> <span>Login</span>
            </Link>
          </li>
        )}
        {loggedInState === LoggedInState.Offline && (
          <li className={s.rightSideItem}>
            <span className={s.disabled}>
              <ConnectionIcon color="grey" /> <span>Offline</span>
            </span>
          </li>
        )}
      </ul>
    </nav>
  )
}

export default Nav
