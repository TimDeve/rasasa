importScripts("/assets-manifest.js");

import { registerRoute } from 'workbox-routing'
import { NetworkOnly } from 'workbox-strategies'
import { Plugin } from 'workbox-background-sync'
import { precacheAndRoute } from 'workbox-precaching'
import { skipWaiting, clientsClaim } from 'workbox-core'

skipWaiting()
clientsClaim()

precacheAndRoute(self.__precacheManifest || [])

const bgSyncPlugin = new Plugin('updateQueue', {
  maxRetentionTime: 48 * 60,
})

registerRoute(
  /\/api\/v0\/.+/,
  new NetworkOnly({
    plugins: [bgSyncPlugin],
  }),
  'PATCH'
)
