importScripts('https://storage.googleapis.com/workbox-cdn/releases/4.2.0/workbox-sw.js')

if (workbox) {
  workbox.precaching.precacheAndRoute(self.__precacheManifest || [])

  const bgSyncPlugin = new workbox.backgroundSync.Plugin('updateQueue', {
    maxRetentionTime: 48 * 60,
  })

  workbox.routing.registerRoute(
    /\/api\/v0\/.+/,
    new workbox.strategies.NetworkOnly({
      plugins: [bgSyncPlugin],
    }),
    'PATCH'
  )
} else {
  console.error('Workbox failed to load.')
}
