import sanitizeHtml from 'sanitize-html'

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

  function transformLinks(tagName, attribs) {
    const href = attribs.href || ''
    if (!href || href.match(/https?/g)) {
      return {
        tagName,
        attribs,
      }
    }

    const newUrl = new URL(pageUrl)

    if (href.charAt(0) === '/') {
      newUrl.pathname = href
    } else {
      newUrl.pathname = `${newUrl.pathname}/${href}`
    }

    return {
      tagName,
      attribs: {
        ...attribs,
        href: newUrl.toString(),
      },
    }
  }

  const sanitizeHtmlOptions = {
    ...sanitizeHtmlDefaultOptions,
    transformTags: {
      img: transformImages,
      a: transformLinks,
    },
  }

  return sanitizeHtml(html, sanitizeHtmlOptions)
}

export default transformHtml
