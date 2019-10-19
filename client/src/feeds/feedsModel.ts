export interface Feed {
  id: number
  name: string
  url: string
}

export interface FeedList {
  id: number
  name: string
  feedIds: number[]
}
