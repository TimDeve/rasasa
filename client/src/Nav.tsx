import React from 'react'
import { Link } from 'react-router-dom'

import s from './Nav.scss'

function Nav() {
  return (
    <nav className={s.nav}>
      <ul>
        <li>
          <Link to="/">Stories</Link>
        </li>
        <li>
          <Link to="/feeds">Feeds</Link>
        </li>
      </ul>
    </nav>
  )
}

export default Nav
