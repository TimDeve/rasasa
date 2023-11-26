import cleanHtml from './cleanHtml.js'

const domains = new Set(['nitter.net', 'twitter.com', 'x.com'])

function url(u) {
  const newUrl = u

  newUrl.host = 'nitter.net'
  newUrl.hostname = 'nitter.net'

  return newUrl
}

function html(pageUrl, doc) {
  const username = doc.document
    .getElementsByTagName('title')
    .map(el => el.textContent)
    .join('')
    .split(':')[0]
  const title = `${username} | Twitter`
  const content = doc.document.getElementsByClassName('conversation').toString()

  const transformedUrl = url(new URL(pageUrl))

  return {
    readable: true,
    title: title,
    url: pageUrl,
    content: cleanHtml(transformedUrl, content),
  }
}

export default {
  url,
  html,
  domains,
}
