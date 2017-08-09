import { combineReducers } from 'redux'
import surveySummaries from './surveySummaries'
import preferredSurvey from './preferredSurvey'

const rootReducer = combineReducers({
	surveySummaries,
	preferredSurvey
})

export default rootReducer