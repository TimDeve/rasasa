import cleanHtml from './cleanHtml.js'
import twitter from './twitter.js'
import { Readability, isProbablyReaderable } from 'readability'
import TurndownService from 'turndown'
import turndownPluginGfm from 'turndown-plugin-gfm'

const turndownService = new TurndownService()
turndownService.use(turndownPluginGfm.gfm)

const transformers = [twitter]

export function transformUrl(u) {
  var newUrl = new URL(u)

  for (const t of transformers) {
    if (t.domains.has(newUrl.host)) {
      if (t.url) {
        return t.url(newUrl).toString()
      }
      break
    }
  }

  return newUrl.toString()
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
