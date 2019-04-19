import { useReducer, Dispatch } from 'react'

import { Story } from './storiesModel'

interface ClearStoriesAction {
  type: 'clear'
}
export const clearStories = (): ClearStoriesAction => ({ type: 'clear' })

interface SetAllStoriesAction {
  type: 'setAll'
  payload: Story[]
}
export const setAllStories = (stories: Story[]): SetAllStoriesAction => ({ type: 'setAll', payload: stories })

interface MarkStoryAsReadAction {
  type: 'markAsRead'
  payload: number
}
export const markStoryAsRead = (storyId: number): MarkStoryAsReadAction => ({
  type: 'markAsRead',
  payload: storyId,
})

interface MarkAllStoriesAsReadAction {
  type: 'markAllAsRead'
}
export const markAllStoriesAsRead = (): MarkAllStoriesAsReadAction => ({ type: 'markAllAsRead' })

interface SetStoriesLoadingAction {
  type: 'setLoading'
  payload: boolean
}
export const setStoriesLoading = (loading: boolean): SetStoriesLoadingAction => ({
  type: 'setLoading',
  payload: loading,
})

type StoriesAction =
  | ClearStoriesAction
  | SetAllStoriesAction
  | MarkStoryAsReadAction
  | MarkAllStoriesAsReadAction
  | SetStoriesLoadingAction

export type StoriesDispatch = Dispatch<StoriesAction>

interface StoryById {
  [key: number]: Story
}

interface State {
  byId: StoryById
  allIds: number[]
  loading: boolean
}

const initialState: State = { loading: true, byId: {}, allIds: [] }

function reducer(state: State, action: StoriesAction): State {
  switch (action.type) {
    case 'setAll':
      const byId: StoryById = {}
      const allIds: number[] = []
      for (const story of action.payload) {
        byId[story.id] = story
        allIds.push(story.id)
      }
      return { ...state, byId, allIds }
    case 'clear':
      return { ...state, byId: {}, allIds: [] }
    case 'markAsRead':
      if (!state.byId[action.payload]) return state

      return {
        ...state,
        byId: {
          ...state.byId,
          [action.payload]: {
            ...state.byId[action.payload],
            isRead: true,
          },
        },
        allIds: state.allIds,
      }
    case 'markAllAsRead':
      const storiesById: StoryById = {}
      for (const storyId of state.allIds) {
        storiesById[storyId] = { ...state.byId[storyId], isRead: true }
      }
      return { ...state, byId: storiesById }
    case 'setLoading':
      return { ...state, loading: action.payload }
    default:
      throw new Error()
  }
}

export function useStories(): [{ stories: Story[]; loading: boolean }, StoriesDispatch] {
  const [state, dispatch] = useReducer(reducer, initialState)

  return [
    {
      stories: state.allIds.map(id => state.byId[id]),
      loading: state.loading,
    },
    dispatch,
  ]
}
