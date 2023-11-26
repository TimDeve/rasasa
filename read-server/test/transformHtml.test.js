import transformHtml from '../src/transform/transformHtml.js'

describe('transformHtml', () => {
  describe('img transformation', () => {
    it('should not touch src attribute with http protocol', () => {
      const html = '<img src="https://www.example.com/pic.jpg" />'
      const expected = '<img src="https://www.example.com/pic.jpg" />'

      const result = transformHtml(html, "Not Important")

      expect(result).toBe(expected)
    })

    it('should append the src to the page url when relative url', () => {
      const html = '<img src="relative/pic.jpg" />'
      const pageUrl = 'https://www.example.com/something'

      const expected = '<img src="https://www.example.com/something/relative/pic.jpg" />'

      const result = transformHtml(html, pageUrl)

      expect(result).toBe(expected)
    })

    it('should replace the path of the url if the src is relative path from the root', () => {
      const html = '<img src="/pic.jpg" />'
      const pageUrl = 'https://www.example.com/something'

      const expected = '<img src="https://www.example.com/pic.jpg" />'

      const result = transformHtml(html, pageUrl)

      expect(result).toBe(expected)
    })
  })
})
