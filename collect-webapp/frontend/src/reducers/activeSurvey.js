import {
  ACTIVE_SURVEY_REQUESTED,
  ACTIVE_SURVEY_FETCHED,
  SELECT_ACTIVE_SURVEY_LANGUAGE,
  ACTIVE_SURVEY_CLEARED,
} from '../actions/activeSurvey'

const initialState = {
  isFetching: false,
  survey: null,
  language: null,
  lastUpdated: null,
}

function activeSurvey(state = initialState, action) {
  switch (action.type) {
    case ACTIVE_SURVEY_CLEARED:
      return { ...initialState }
    case ACTIVE_SURVEY_REQUESTED:
      return { ...state, isFetching: true }
    case ACTIVE_SURVEY_FETCHED:
      const { survey, language } = action
      return { ...state, isFetching: false, survey, language, lastUpdated: action.receivedAt }
    case SELECT_ACTIVE_SURVEY_LANGUAGE:
      return { ...state, language: action.language }
    default:
      return state
  }
}

export default activeSurvey
