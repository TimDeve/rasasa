const redis = require('redis')
const bluebird = require('bluebird')
const fetch = require('node-fetch')
const createError = require('http-errors');
const { JSDOM } = require('jsdom')
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

  return contentType.includes('text/html') ||
         contentType.includes('text/plain')
}

fastify.get('/v0/read', async (request, reply) => {
  const { page } = request.query

  if (!page) {
    reply.code(400)
    return '"page" query parameter is missing'
  }

  const cachedPage = await redisClient.getAsync(prefixPageCaching(page))
  if (cachedPage) {
    reply.type('application/json').code(200)
    return JSON.parse(cachedPage)
  }

  const res = await fetch(page)
  if (!res.ok) {
    return new createError.BadGateway()
  }

  const contentType = res.headers.get('content-type');
  if (!isValidContentType(contentType)) {
    reply.type('application/json').code(200)
    const payload = { readable: false, url: page }
    cacheResponse(page, payload)
    return payload
  }

  const text = await res.text()
  const doc = new JSDOM(text, {
    page,
  })

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

fastify.listen(readUrl.port, (err, address) => {
  if (err) throw err
  fastify.log.info(`server listening on ${address}`)
})
