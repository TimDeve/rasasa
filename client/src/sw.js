const filesToCache = ['/', 'index.css', 'index.js']

self.addEventListener('install', event => {
  console.log('Attempting to install service worker and cache static assets')
  event.waitUntil(
    caches.open('rasasa-static').then(cache => {
      return cache.addAll(filesToCache)
    })
  )
})

self.addEventListener('fetch', function(event) {
  event.respondWith(
    caches.open('rasasa-dynamic').then(function(cache) {
      return cache.match(event.request).then(function(response) {
        return (
          response ||
          fetch(event.request).then(function(response) {
            cache.put(event.request, response.clone())
            return response
          })
        )
      })
    })
  )
})
