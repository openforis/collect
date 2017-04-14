import { combineReducers } from 'redux'
import {
  REQUEST_SURVEY_SUMMARIES, RECEIVE_SURVEY_SUMMARIES, INVALIDATE_SURVEY_SUMMARIES,
  SELECT_PREFERRED_SURVEY, INVALIDATE_SURVEY
} from '../actions'

const preferredSurvey = (state = null, action) => {
  switch (action.type) {
    case SELECT_PREFERRED_SURVEY:
      return action.survey
    default:
      return state
  }
}

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

const rootReducer = combineReducers({
  surveySummaries,
  preferredSurvey
})

export default rootReducer