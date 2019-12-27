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
          <Link to="/lists"><ListIcon/> Lists</Link>
        </li>
        <li>
          <Link to="/feeds"><FeedIcon/> Feeds</Link>
        </li>
      </ul>
      <ul>
        {loggedInState === LoggedInState.LoggedOut && (
          <li>
            <Link to="/login"><ConnectionIcon/> Login</Link>
          </li>
        )}
        {loggedInState === LoggedInState.Offline && (
          <li>
            <span className={s.disabled}><ConnectionIcon color='grey' /> Offline</span>
          </li>
        )}
      </ul>
    </nav>
  )
}

export default Nav
