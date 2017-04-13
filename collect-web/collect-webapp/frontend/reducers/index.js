import { combineReducers } from 'redux'
import {
  REQUEST_SURVEY_SUMMARIES, RECEIVE_SURVEY_SUMMARIES, INVALIDATE_SURVEY_SUMMARIES,
  SELECT_PREFERRED_SURVEY, INVALIDATE_SURVEY,
  REQUEST_RECORDS, RECEIVE_RECORDS
} from '../actions'

const selectedPreferredSurvey = (state = '', action) => {
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

const records = (state = {
  isFetching: false,
  didInvalidate: false,
  items: []
}, action) => {
  switch (action.type) {
    case INVALIDATE_SURVEY:
      return {
        ...state,
        didInvalidate: true
      }
    case REQUEST_RECORDS:
      return {
        ...state,
        isFetching: true,
        didInvalidate: false
      }
    case RECEIVE_RECORDS:
      return {
        ...state,
        isFetching: false,
        didInvalidate: false,
        items: action.records,
        lastUpdated: action.receivedAt
      }
    default:
      return state
  }
}

const recordsBySurvey = (state = { }, action) => {
  switch (action.type) {
    case INVALIDATE_SURVEY:
    case RECEIVE_RECORDS:
    case REQUEST_RECORDS:
      return {
        ...state,
        [action.survey]: records(state[action.survey], action)
      }
    default:
      return state
  }
}

const rootReducer = combineReducers({
  surveySummaries,
  recordsBySurvey,
  selectedPreferredSurvey
})

export default rootReducer