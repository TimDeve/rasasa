const filesToCache = ['/', 'index.css', 'index.js']

self.addEventListener('install', event => {
  event.waitUntil(
    caches.open('rasasa-static').then(cache => {
      return cache.addAll(filesToCache)
    })
  )
})

self.addEventListener('fetch', function(event) {
  const requestUrl = new URL(event.request.url)

  if (requestUrl.origin === location.origin) {
    event.respondWith(
      caches.open('rasasa-dynamic').then(function(cache) {
        return fetch(event.request)
          .then(function(response) {
            cache.put(event.request, response.clone())
            return response
          })
          .catch(function() {
            return cache.match(event.request)
          })
      })
    )
  }
})
