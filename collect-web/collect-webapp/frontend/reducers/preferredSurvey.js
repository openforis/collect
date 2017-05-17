import {
  REQUEST_FULL_PREFERRED_SURVEY, 
  RECEIVE_FULL_PREFERRED_SURVEY, 
  INVALIDATE_PREFERRED_SURVEY
} from '../actions'

const preferredSurvey = (state = null, action) => {
  switch (action.type) {
    case REQUEST_FULL_PREFERRED_SURVEY:
      return {
        ...state,
        isFetching: true,
        didInvalidate: false
      }
    case RECEIVE_FULL_PREFERRED_SURVEY:
        return {
          ...state,
          isFetching: false,
          didInvalidate: false,
          survey: action.survey,
          lastUpdated: action.receivedAt
        }
    default:
      return state
  }
}

export default preferredSurvey