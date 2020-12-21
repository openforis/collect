import {
  REQUEST_FULL_ACTIVE_SURVEY,
  RECEIVE_FULL_ACTIVE_SURVEY,
  INVALIDATE_ACTIVE_SURVEY,
  SELECT_ACTIVE_SURVEY_LANGUAGE,
} from '../actions/activeSurvey'

const initialState = {
  isFetching: false,
  didInvalidate: false,
  survey: null,
  language: null,
}

function activeSurvey(state = initialState, action) {
  switch (action.type) {
    case INVALIDATE_ACTIVE_SURVEY:
      return Object.assign({}, state, {
        didInvalidate: true,
      })
    case REQUEST_FULL_ACTIVE_SURVEY:
      return Object.assign({}, state, {
        isFetching: true,
        didInvalidate: false,
      })
    case RECEIVE_FULL_ACTIVE_SURVEY:
      const { survey, language } = action
      return Object.assign({}, state, {
        isFetching: false,
        didInvalidate: false,
        survey,
        language,
        lastUpdated: action.receivedAt,
      })
    case SELECT_ACTIVE_SURVEY_LANGUAGE:
      return Object.assign({}, state, {
        language: action.language,
      })
    default:
      return state
  }
}

export default activeSurvey
