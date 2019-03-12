import React from 'react'
import { BrowserRouter as Router, Route } from 'react-router-dom'

import { Hello } from './hello'

function App() {
  return (
    <Router>
      <Route path="/" component={Hello} />
    </Router>
  )
}

export default App
