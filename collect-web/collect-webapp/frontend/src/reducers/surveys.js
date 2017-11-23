import update from 'react-addons-update';

import Arrays from 'utils/Arrays'

import {
  REQUEST_SURVEY_SUMMARIES, RECEIVE_SURVEY_SUMMARIES, INVALIDATE_SURVEY_SUMMARIES, SURVEY_USER_GROUP_CHANGED, 
  NEW_SURVEY_CREATED, SURVEY_CREATION_ERROR
} from 'actions/surveys'

function surveySummaries(
  state = {
    isFetching: false,
    didInvalidate: false,
    items: [],
    surveyCreationErrors: []
  },
  action
) {
  switch (action.type) {
    case INVALIDATE_SURVEY_SUMMARIES:
      return Object.assign({}, state, {
        didInvalidate: true
      })
    case REQUEST_SURVEY_SUMMARIES:
      return Object.assign({}, state, {
        isFetching: true,
        didInvalidate: false
      })
    case RECEIVE_SURVEY_SUMMARIES:
      return Object.assign({}, state, {
        isFetching: false,
        didInvalidate: false,
        items: action.summaries,
        lastUpdated: action.receivedAt
      })
    case SURVEY_USER_GROUP_CHANGED:
      const oldItems = state.items
      const itemIdx = oldItems.findIndex(s => s.id === action.surveyId)
      const oldItem = oldItems[itemIdx]
      const newItem = Object.assign({}, oldItem, {
        userGroupId: action.newUserGroupId
      })
      const newItems = update(oldItems, {
        $splice: [[itemIdx, 1, newItem]]
      })
      return Object.assign({}, state, {
        items: newItems,
        lastUpdated: action.receivedAt
      })
    case NEW_SURVEY_CREATED: {
      return Object.assign({}, state, {
        surveyCreationErrors: [],
        items: Arrays.addItem(state.items, action.summary)
      })
    }
    case SURVEY_CREATION_ERROR: {
      return Object.assign({}, state, {
        surveyCreationErrors: action.errors
      })
    }
    default:
      return state
  }
}

export default surveySummaries