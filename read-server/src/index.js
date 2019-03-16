const fetch = require('node-fetch')
const { JSDOM } = require('jsdom')
const Readability = require('readability')
const { isProbablyReaderable } = require('readability/Readability-readerable')

const fastify = require('fastify')({
  logger: true,
})

fastify.get('/v0/read', async (request, reply) => {
  const { page } = request.query

  if (!page) {
    reply.code(400)
    return '"page" query parameter is missing'
  }

  const res = await fetch(page)

  const text = await res.text()

  const doc = new JSDOM(text, {
    page,
  })

  const readable = isProbablyReaderable(doc.window.document)
  if (!readable) {
    reply.type('application/json').code(200)
    return { readable: false }
  }

  const reader = new Readability(doc.window.document)
  const article = reader.parse()

  reply.type('application/json').code(200)
  return {
    readable,
    title: article.title,
    byline: article.byline,
    content: article.content,
  }
})

fastify.listen(8092, (err, address) => {
  if (err) throw err
  fastify.log.info(`server listening on ${address}`)
})
