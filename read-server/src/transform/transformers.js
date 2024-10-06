import cleanHtml from './cleanHtml.js'
import twitter from './twitter.js'
import { Readability, isProbablyReaderable } from 'readability'
import TurndownService from 'turndown'
import turndownPluginGfm from 'turndown-plugin-gfm'

const turndownService = new TurndownService()
turndownService.use(turndownPluginGfm.gfm)

const transformers = [twitter]

const paywalledDomains = new Set(['www.nytimes.com', 'newyorker.com', 'aftermath.site', 'www.theguardian.com'])

export function transformUserAgent(url) {
  const parsedUrl = new URL(url)

  return paywalledDomains.has(parsedUrl.host)
    // Pretend to be browser/googlebot
    ? 'Mozilla/5.0 (Linux; Android 6.0.1; Nexus 5X Build/MMB29P) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/127.0.6533.119 Mobile Safari/537.36 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)'
    : 'Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:120.0) Gecko/20100101 Firefox/120.0'
}

export function transformUrl(u) {
  const parsedUrl = new URL(u)

  for (const t of transformers) {
    if (t.domains.has(parsedUrl.host)) {
      if (t.url) {
        return t.url(parsedUrl).toString()
      }
      break
    }
  }

  return parsedUrl.toString()
}

export function transformHtml(url, doc, format) {
  for (const t of transformers) {
    if (t.domains.has(new URL(url).host)) {
      if (t.html) {
        return t.html(url, doc)
      }
      break
    }
  }

  const readable = isProbablyReaderable(doc.window.document)
  if (!readable) {
    return { readable: false, url }
  }

  const reader = new Readability(doc.window.document)
  const article = reader.parse()
  const content = cleanHtml(url, article.content)

  const payload = {
    readable: true,
    title: article.title,
    byline: article.byline,
    contentFormat: format,
    url,
  }

  if (format === 'markdown') {
    payload.content = turndownService.turndown(content)
  } else {
    payload.content = content
  }

  return payload
}
