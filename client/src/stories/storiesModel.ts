import { Feed } from '../feeds/feedsModel'

export interface Article {
  readable: boolean
  title?: String
  byline?: String
  content?: string
}

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
