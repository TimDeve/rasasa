const redis = require('redis')
const bluebird = require('bluebird')
const fetch = require('node-fetch')
const { JSDOM } = require('jsdom')
const Readability = require('readability')
const { isProbablyReaderable } = require('readability/Readability-readerable')

const REDIS_PREFIX = 'rasasa-read'

bluebird.promisifyAll(redis.RedisClient.prototype)
bluebird.promisifyAll(redis.Multi.prototype)
const redisClient = redis.createClient()

const fastify = require('fastify')({
  logger: true,
})

function prefixPageCaching(pageUrl) {
  return `${REDIS_PREFIX}:page-caching:${pageUrl}`
}

function cacheResponse(pageUrl, response) {
  redisClient.set(prefixPageCaching(pageUrl), JSON.stringify(response), 'EX', 60 * 60 * 24)
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

  const text = await res.text()

  const doc = new JSDOM(text, {
    page,
  })

  const readable = isProbablyReaderable(doc.window.document)
  if (!readable) {
    reply.type('application/json').code(200)
    const payload = { readable: false }
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
    content: article.content,
  }
  cacheResponse(page, payload)
  return payload
})

fastify.listen(8092, (err, address) => {
  if (err) throw err
  fastify.log.info(`server listening on ${address}`)
})
