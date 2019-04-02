import React from 'react'
import ReactDOM from 'react-dom'

import './reset.scss'
import './base.scss'
import App from './App'

ReactDOM.render(<App />, document.getElementById('root'))

if ('serviceWorker' in navigator) {
  navigator.serviceWorker.register('/sw.js').then(function() {
    console.log('Service Worker Registered')
  })
}
