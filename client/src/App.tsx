import './hotReloaderConfig'

import React from 'react'
import { hot } from 'react-hot-loader/root'
import { BrowserRouter as Router, Navigate, Route, Routes } from 'react-router-dom'

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
            <Routes>
              <Route path="/login" element={<LoginPage />} />
              <Route path="/" element={<StoriesPage />} />
              <Route path="/lists/:listId/*" element={<StoriesPage />} />
              <Route path="/stories/:storyId/*" element={<StoriesPage />} />
              <Route path="/feeds" element={<FeedsPage />} />
              <Route path="/lists" element={<ListsPage />} />
              <Route path="*" element={<Navigate to="/" />} />
            </Routes>
          </>
        </Router>
      </div>
    </ProvidersProvider>
  )
}

export default hot(App)
