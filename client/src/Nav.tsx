import React from 'react'
import { Link } from 'react-router-dom'

function Nav() {
  return (
    <nav>
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
