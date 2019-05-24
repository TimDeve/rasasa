const sanitizeHtml = require('sanitize-html')

const sanitizeHtmlDefaultOptions = {
  allowedTags: [...sanitizeHtml.defaults.allowedTags, 'h1', 'h2', 'img', 'video', 'picture', 'source'],
  allowedAttributes: {
    ...sanitizeHtml.defaults.allowedAttributes,
    video: ['src', 'type'],
    source: ['srcset', 'media', 'type'],
  },
  allowedIframeHostnames: ['www.youtube.com'],
}

function transformHtml(html, pageUrl) {
  function transformImages(tagName, attribs) {
    const src = attribs.src || ''
    if (!src || src.match(/https?/g)) {
      return {
        tagName,
        attribs,
      }
    }

    const newUrl = new URL(pageUrl)

    if (src.charAt(0) === '/') {
      newUrl.pathname = src
    } else {
      newUrl.pathname = `${newUrl.pathname}/${src}`
    }

    return {
      tagName,
      attribs: {
        ...attribs,
        src: newUrl.toString(),
      },
    }
  }

  const sanitizeHtmlOptions = {
    ...sanitizeHtmlDefaultOptions,
    transformTags: {
      img: transformImages,
    },
  }

  return sanitizeHtml(html, sanitizeHtmlOptions)
}

module.exports = transformHtml
