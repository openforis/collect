import { combineReducers } from 'redux'
import currentJob from './currentJob'
import login from './login'
import session from './session'
import surveySummaries from './surveySummaries'
import preferredSurvey from './preferredSurvey'
import records from './records'
import users from './users'
import userGroups from './userGroups'

const rootReducer = combineReducers({
	currentJob,
	login,
	session,
	preferredSurvey,
	records,
	surveySummaries,
	users,
	userGroups
})

export default rootReducer