import { combineReducers } from 'redux'
import login from './login'
import session from './session'
import surveySummaries from './surveySummaries'
import preferredSurvey from './preferredSurvey'
import users from './users'

const rootReducer = combineReducers({
	login,
	session,
	preferredSurvey,
	surveySummaries,
	users
})

export default rootReducer