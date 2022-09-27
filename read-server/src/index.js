const redis = require('redis')
const bluebird = require('bluebird')
const fetch = require('node-fetch')
const createError = require('http-errors')
const { parseHTML } = require('linkedom')
const { Readability, isProbablyReaderable } = require('readability')

const transformHtml = require('./transformHtml')

bluebird.promisifyAll(redis.RedisClient.prototype)
bluebird.promisifyAll(redis.Multi.prototype)

const REDIS_PREFIX = 'rasasa-read'

const redisClient = redis.createClient(process.env['REDIS_URL'])

const fastify = require('fastify')({
  logger: { prettyPrint: true },
})

function prefixPageCaching(pageUrl) {
  return `${REDIS_PREFIX}:page-caching:${pageUrl}`
}

function cacheResponse(pageUrl, response) {
  redisClient.set(prefixPageCaching(pageUrl), JSON.stringify(response), 'EX', 60 * 60 * 24)
}

function isValidContentType(contentType) {
  if (!contentType) {
    return false
  }

  return contentType.includes('text/html') || contentType.includes('text/plain')
}

fastify.get('/v0/read', async (request, reply) => {
  const { page, skipCache } = request.query

  if (!page) {
    reply.code(400)
    return '"page" query parameter is missing'
  }

  if (!skipCache) {
    const cachedPage = await redisClient.getAsync(prefixPageCaching(page))
    if (cachedPage) {
      reply.type('application/json').code(200)
      return JSON.parse(cachedPage)
    }
  }

  const res = await fetch(page, {
    headers: {
      // Pretend to be a browser
      Accept: 'text/html,application/xhtml+xml',
      'User-Agent': 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:105.0) Gecko/20100101 Firefox/105.0',
    },
  })
  if (!res.ok) {
    let body = null
    try {
      body = await res.text()
    } catch (_) {}

    request.log
      .child({
        status: res.status,
        statusText: res.statusText,
        headers: res.headers,
        body: body,
      })
      .error('Remote fetch has failed')
    return new createError.BadGateway(`Remote response status was: ${res.status}`)
  }

  const contentType = res.headers.get('content-type')
  if (!isValidContentType(contentType)) {
    reply.type('application/json').code(200)
    const payload = { readable: false, url: page }
    cacheResponse(page, payload)
    return payload
  }

  const text = await res.text()
  const doc = parseHTML(text)

  const readable = isProbablyReaderable(doc.window.document)
  if (!readable) {
    reply.type('application/json').code(200)
    const payload = { readable: false, url: page }
    cacheResponse(page, payload)
    return payload
  }

  const reader = new Readability(doc.window.document)
  const article = reader.parse()

  reply.type('application/json').code(200)
  const payload = {
    readable,
    title: article.title,
    byline: article.byline,
    content: transformHtml(article.content, page),
    url: page,
  }
  cacheResponse(page, payload)
  return payload
})

const readUrl = new URL(process.env['READ_URL'])

fastify.listen(readUrl.port, readUrl.hostname, (err, address) => {
  if (err) throw err
  fastify.log.info(`server listening on ${address}`)
})
