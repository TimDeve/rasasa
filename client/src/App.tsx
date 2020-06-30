import './hotReloaderConfig'

import React from 'react'
import { hot } from 'react-hot-loader/root'
import { BrowserRouter as Router, Redirect, Route, Switch } from 'react-router-dom'

import FeedsPage from 'feeds/FeedsPage'
import ListsPage from 'feeds/ListsPage'
import Nav from './Nav'
import StoriesPage from 'stories/StoriesPage'
import LoginPage from 'login/LoginPage'
import { FeedsProvider } from 'feeds/feedsContext'
import { LoggedInProvider } from 'shared/loggedInState/LoggedInContext'
import ProvidersProvider from 'shared/ProvidersProvider'
import { Helmet } from 'react-helmet'
import s from './App.scss'

function App() {
  return (
    <ProvidersProvider providers={[LoggedInProvider, FeedsProvider]}>
      <div className={s.root}>
        <Helmet>
          <title>Rasasa</title>
        </Helmet>
        <Router>
          <>
            <Nav />
            <Switch>
              <Route exact path="/login" component={LoginPage} />
              <Route exact path="/" component={StoriesPage} />
              <Route path="(/stories/:storyId|/lists/:listId)" component={StoriesPage} />
              <Route exact path="/feeds" component={FeedsPage} />
              <Route exact path="/lists" component={ListsPage} />
              <Redirect to="/" />
            </Switch>
          </>
        </Router>
      </div>
    </ProvidersProvider>
  )
}

export default hot(App)
