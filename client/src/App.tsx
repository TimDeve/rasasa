import './hotReloaderConfig'

import React from 'react'
import { hot } from 'react-hot-loader/root'
import { BrowserRouter as Router, Route } from 'react-router-dom'

import FeedsPage from 'pages/FeedsPage'
import Nav from './Nav'
import StoriesPage from 'pages/StoriesPage'
import StoryPage from 'pages/StoryPage'

function App() {
  return (
    <Router>
      <>
        <Nav />
        <Route exact path="/" component={StoriesPage} />
        <Route exact path="/feeds" component={FeedsPage} />
        <Route exact path="/story" component={StoryPage} />
      </>
    </Router>
  )
}

export default hot(App)
