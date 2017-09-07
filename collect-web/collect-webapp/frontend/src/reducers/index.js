import { combineReducers } from 'redux'
import login from './login'
import session from './session'
import surveySummaries from './surveySummaries'
import preferredSurvey from './preferredSurvey'
import users from './users'
import userGroups from './userGroups'

const rootReducer = combineReducers({
	login,
	session,
	preferredSurvey,
	surveySummaries,
	users,
	userGroups
})

export default rootReducer