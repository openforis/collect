import { combineReducers } from 'redux'

import currentJob from './currentJob'
import applicationInfo from './applicationInfo'
import login from './login'
import session from './session'
import sidebar from './sidebar'
import newSurvey from 'surveydesigner/newSurvey/reducer'
import surveyImport from 'surveydesigner/surveyImport/reducer'
import surveysList from 'surveydesigner/surveysList/reducer'
import activeSurvey from './activeSurvey'
import recordDataTable from 'datamanagement/recordDataTable/reducer'
import users from './users'
import userGroups from './userGroups'
import systemError from './systemError'

const rootReducer = combineReducers({
  currentJob,
  applicationInfo,
  login,
  session,
  sidebar,
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
  userGroups,
  systemError,
})

export default rootReducer
