import React from 'react'
import ReactDOM from 'react-dom'
import { Workbox } from 'workbox-window'
import { scriptUrl } from 'service-worker-loader!./sw'

import './reset.scss'
import './base.scss'
import App from './App'

ReactDOM.render(<App />, document.getElementById('root'))

if ('serviceWorker' in navigator) {
  const wb = new Workbox(scriptUrl)

  wb.addEventListener('activated', event => {
    if (!event.isUpdate) {
      console.log('Service worker updated')
    }
  })

  wb.register()
}
