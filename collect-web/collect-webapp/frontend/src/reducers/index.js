import { combineReducers } from 'redux'
import { reducer as formReducer } from 'redux-form'
import currentJob from './currentJob'
import applicationInfo from './applicationInfo'
import login from './login'
import session from './session'
import surveys from './surveys'
import surveySummaries from './surveySummaries'
import preferredSurvey from './preferredSurvey'
import records from './records'
import users from './users'
import userGroups from './userGroups'

const rootReducer = combineReducers({
	form: formReducer, //react-form reducer
	currentJob,
	applicationInfo,
	login,
	session,
	preferredSurvey,
	records,
	surveys,
	surveySummaries,
	users,
	userGroups
})

export default rootReducer