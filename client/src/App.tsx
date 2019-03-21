import './hotReloaderConfig'

import React from 'react'
import { hot } from 'react-hot-loader/root'
import { BrowserRouter as Router, Redirect, Route, Switch } from 'react-router-dom'

import FeedsPage from 'pages/FeedsPage'
import Nav from './Nav'
import StoriesPage from 'pages/StoriesPage'
import StoryPage from 'pages/StoryPage'
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
            <Route exact path="/story" component={StoryPage} />
            <Redirect to="/" />
          </Switch>
        </>
      </Router>
    </div>
  )
}

export default hot(App)
