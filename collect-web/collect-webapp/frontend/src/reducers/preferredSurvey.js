import {
  SELECT_PREFERRED_SURVEY,
  REQUEST_FULL_PREFERRED_SURVEY, 
  RECEIVE_FULL_PREFERRED_SURVEY, 
  INVALIDATE_PREFERRED_SURVEY
} from '../actions'

const initialState = {
	isFetching: false,
	didInvalidate: false,
	survey: null
}

function preferredSurvey(
	state = initialState,
	action
) {
  switch (action.type) {
  	case SELECT_PREFERRED_SURVEY:
  		return action.survey
  	case INVALIDATE_PREFERRED_SURVEY:
    	return Object.assign({}, state, {
    		didInvalidate: true
    	})
    case REQUEST_FULL_PREFERRED_SURVEY:
        return Object.assign({}, state, {
	        isFetching: true,
	        didInvalidate: false
        })
    case RECEIVE_FULL_PREFERRED_SURVEY:
        return Object.assign({}, state, {
		    isFetching: false,
		    didInvalidate: false,
		    survey: action.survey,
		    lastUpdated: action.receivedAt
        })
    default:
    	return state
  }
}

export default preferredSurvey