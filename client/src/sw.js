importScripts('https://storage.googleapis.com/workbox-cdn/releases/4.2.0/workbox-sw.js')

if (workbox) {
  workbox.precaching.precacheAndRoute(self.__precacheManifest || [])
} else {
  console.error('Workbox failed to load.')
}
