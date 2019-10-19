import Dexie from 'dexie'

import { Feed, FeedList } from './feedsModel'

export class FeedsDatabase extends Dexie {
  feeds!: Dexie.Table<Feed, number>
  feedLists!: Dexie.Table<FeedList, number>

  constructor() {
    super('FeedsDatabase')

    this.version(1).stores({
      feeds: 'id',
      feedLists: 'id',
    })

    this.feeds = this.table('feeds')
    this.feedLists = this.table('feedLists')
  }
}

export default new FeedsDatabase()
