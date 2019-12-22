import React, { useContext } from 'react'
import { Link } from 'react-router-dom'

import { LoggedInContext, LoggedInState } from 'shared/loggedInState/LoggedInContext'
import s from './Nav.scss'

function Nav() {
  const loggedInState = useContext(LoggedInContext)

  return (
    <nav className={s.nav}>
      <ul>
        <li>
          <Link to="/lists">Lists</Link>
        </li>
        <li>
          <Link to="/feeds">Feeds</Link>
        </li>
      </ul>
      <ul>
        {loggedInState === LoggedInState.LoggedOut && (
          <li>
            <Link to="/login">Login</Link>
          </li>
        )}
        {loggedInState === LoggedInState.Offline && (
          <li>
            <span className={s.disabled}>Offline</span>
          </li>
        )}
      </ul>
    </nav>
  )
}

export default Nav
