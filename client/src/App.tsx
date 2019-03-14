import './hotReloaderConfig'

import React from 'react'
import { hot } from 'react-hot-loader/root'
import { BrowserRouter as Router, Route } from 'react-router-dom'

import FeedsPage from './FeedsPage'
import Nav from './Nav'
import StoriesPage from './StoriesPage'

function App() {
  return (
    <Router>
      <>
        <Nav />
        <Route exact path="/" component={StoriesPage} />
        <Route path="/feeds" component={FeedsPage} />
      </>
    </Router>
  )
}

export default hot(App)
