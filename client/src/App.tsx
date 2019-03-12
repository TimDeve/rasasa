import './hotReloaderConfig'

import React from 'react'
import { BrowserRouter as Router, Route } from 'react-router-dom'
import { hot } from 'react-hot-loader/root'

import StoriesPage from './StoriesPage'

function App() {
  return (
    <Router>
      <Route path="/" component={StoriesPage} />
    </Router>
  )
}

export default hot(App)
