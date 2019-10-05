importScripts("/assets-manifest.js");

import { registerRoute } from 'workbox-routing'
import { NetworkOnly } from 'workbox-strategies'
import { Plugin } from 'workbox-background-sync'
import { precacheAndRoute } from 'workbox-precaching'
import { skipWaiting, clientsClaim } from 'workbox-core'

import db from 'stories/StoriesDb'

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

async function retrieveAndStoreArticles(event) {
  const bgFetch = event.registration

  if (bgFetch.id.includes("articles-fetch")) {
    event.waitUntil(
      (async function () {
        const articleRequests = await bgFetch.matchAll();

        const articleResponses = articleRequests.map(
          async articleRequest => {
            const response = await articleRequest.responseReady;
            if (response.ok) {
              const article = await response.json()
              try {
                await db.articles.add({ ...article, timestamp: new Date().getTime() })
              } catch { }
            }
            else {
              console.error("Failed to fetch", response)
            }
          }
        )

        Promise.all(articleResponses)
      })()
    )
  }
}

addEventListener('backgroundfetchsuccess', event => {
  console.log('[Service Worker]: Background Fetch Success', event.registration);

  retrieveAndStoreArticles(event)
})

addEventListener('backgroundfetchfail', event => {
  console.error('[Service Worker]: Background Fetch Error', event.registration);

  retrieveAndStoreArticles(event)
})

addEventListener('backgroundfetchabort', event => {
  console.error('[Service Worker]: Background Fetch Abort', event.registration);

  retrieveAndStoreArticles(event)
})
