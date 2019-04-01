import './hotReloaderConfig'

import React from 'react'
import { hot } from 'react-hot-loader/root'
import { BrowserRouter as Router, Redirect, Route, Switch } from 'react-router-dom'

import FeedsPage from 'feeds/FeedsPage'
import Nav from './Nav'
import StoriesPage from 'stories/StoriesPage'
import StoryPage from 'stories/StoryPage'
import s from './App.scss'

function App() {
  return (
    <div className={s.root}>
      <Router>
        <>
          <Nav />
          <Switch>
            <Route exact path="/" component={StoriesPage} />
            <Route exact path="/feeds" component={FeedsPage} />
            <Route exact path="/story/:storyId" component={StoryPage} />
            <Redirect to="/" />
          </Switch>
        </>
      </Router>
    </div>
  )
}

export default hot(App)
