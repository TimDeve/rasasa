import Dexie from 'dexie'

import { Story, Article } from './storiesModel'

interface Timestamp {
  timestamp: number
}

type ArticleWithTimestamp = Article & Timestamp

export class StoriesDatabase extends Dexie {
  stories!: Dexie.Table<Story, number>
  articles!: Dexie.Table<ArticleWithTimestamp, string>

  constructor() {
    super('StoriesDatabase')

    this.version(1).stores({
      stories: 'id, publishedDate',
      articles: 'url, timestamp',
    })

    this.stories = this.table('stories')
    this.articles = this.table('articles')
  }
}

export default new StoriesDatabase()
