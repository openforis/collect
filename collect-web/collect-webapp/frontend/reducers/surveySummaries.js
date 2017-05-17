import {
  REQUEST_SURVEY_SUMMARIES, RECEIVE_SURVEY_SUMMARIES, INVALIDATE_SURVEY_SUMMARIES
} from '../actions'

const surveySummaries = (state = {
  isFetching: false,
  didInvalidate: false,
  items: []
}, action) => {
  switch (action.type) {
    case INVALIDATE_SURVEY_SUMMARIES:
      return {
        ...state,
        didInvalidate: true
      }
    case REQUEST_SURVEY_SUMMARIES:
      return {
        ...state,
        isFetching: true,
        didInvalidate: false
      }
    case RECEIVE_SURVEY_SUMMARIES:
      return {
        ...state,
        isFetching: false,
        didInvalidate: false,
        items: action.summaries,
        lastUpdated: action.receivedAt
      }
    default:
      return state
  }
}

export default surveySummaries