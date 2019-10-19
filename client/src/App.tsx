import './hotReloaderConfig'

import React from 'react'
import { hot } from 'react-hot-loader/root'
import { BrowserRouter as Router, Redirect, Route, Switch } from 'react-router-dom'

import FeedsPage from 'feeds/FeedsPage'
import Nav from './Nav'
import StoriesPage from 'stories/StoriesPage'
import LoginPage from 'login/LoginPage'
import { FeedsProvider } from 'feeds/feedsContext'
import s from './App.scss'

function App() {
  return (
    <FeedsProvider>
      <div className={s.root}>
        <Router>
          <>
            <Nav />
            <Switch>
              <Route exact path="/login" component={LoginPage} />
              <Route exact path="/(|story/:storyId)" component={StoriesPage} />
              <Route exact path="/feeds" component={FeedsPage} />
              <Redirect to="/" />
            </Switch>
          </>
        </Router>
      </div>
    </FeedsProvider>
  )
}

export default hot(App)
