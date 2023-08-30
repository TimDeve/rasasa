interface ArticleNotReadable {
  readable: false
}

interface ArticleReadable {
  readable: true
  title: string
  byline: string
  content: string
}

export type Article =
  | ArticleReadable
  | (ArticleNotReadable & {
      url: string
    })

export interface Story {
  id: number
  isRead: boolean
  url: string
  title: string
  content: string
  publishedDate: string
  feedId: number
  article?: Article
}
