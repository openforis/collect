import { combineReducers } from 'redux'
//import { reducer as formReducer } from 'redux-form'

import currentJob from './currentJob'
import applicationInfo from './applicationInfo'
import login from './login'
import session from './session'
import newSurvey from 'surveydesigner/newSurvey/reducer'
import surveyImport from 'surveydesigner/surveyImport/reducer'
import surveysList from 'surveydesigner/surveysList/reducer'
import activeSurvey from './activeSurvey'
import recordDataTable from 'datamanagement/recordDataTable/reducer'
import users from './users'
import userGroups from './userGroups'

const rootReducer = combineReducers({
	//form: formReducer, //redux-form reducer
	currentJob,
	applicationInfo,
	login,
	session,
	activeSurvey,
	dataManagement: combineReducers({
		recordDataTable,
	}),
	surveyDesigner: combineReducers({
		newSurvey,
		surveyImport,
		surveysList,
	}),
	users,
	userGroups
})

export default rootReducer