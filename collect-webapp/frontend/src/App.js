import React from 'react'
import { Switch, Route } from 'react-router-dom'

import Header from 'common/components/Header'
import Sidebar from 'common/components/Sidebar'
import CurrentJobMonitorDialog from 'common/containers/CurrentJobMonitorDialog'
import HomePage from 'scenes/HomePage'
import BackupDataExportPage from 'datamanagement/pages/BackupDataExportPage'
import BackupDataImportPage from 'datamanagement/pages/BackupDataImportPage'
import CsvDataExportPage from 'datamanagement/pages/CsvDataExportPage'
import CsvDataImportPage from 'datamanagement/pages/CsvDataImportPage'
import OldClientRecordEditPage from 'datamanagement/pages/OldClientRecordEditPage'
import RecordEditPage from 'datamanagement/pages/RecordEditPage'
import BackupPage from 'backuprestore/pages/BackupPage'
import DashboardPage from 'scenes/DashboardPage'
import DataCleansingPage from 'scenes/DataCleansingPage'
import DataManagementPage from 'datamanagement/pages/DataManagementPage'
import MapPage from 'scenes/MapPage'
import RestorePage from 'backuprestore/pages/RestorePage'
import SaikuPage from 'scenes/SaikuPage'
import SurveyClonePage from 'surveydesigner/pages/SurveyClonePage'
import SurveyEditPage from 'surveydesigner/pages/SurveyEditPage'
import SurveysListPage from 'surveydesigner/pages/SurveysListPage'
import SurveyDataEntryPreviewPage from 'surveydesigner/pages/SurveyDataEntryPreviewPage'
import NewSurveyPage from 'surveydesigner/newSurvey/pages/NewSurveyPage'
import SurveyExportPage from 'surveydesigner/pages/SurveyExportPage'
import SurveyImportPage from 'surveydesigner/surveyImport/pages/SurveyImportPage'
import UsersPage from 'security/pages/UsersPage'
import UserGroupsPage from 'security/pages/UserGroupsPage'
import UserGroupDetailsPage from 'security/pages/UserGroupDetailsPage'
import PasswordChangePage from 'security/pages/PasswordChangePage'

import AppWebSocket from 'ws/appWebSocket'

export const DefaultRoute = ({ component: Component, ...rest }) => (
  <Route
    {...rest}
    component={(props) => (
      <div className="app">
        <Header />
        <div className="app-body">
          <Sidebar />
          <main className="main">
            <div className="main-content-wrapper">
              <Component {...props} />
            </div>
          </main>
        </div>
      </div>
    )}
  />
)

export const FullScreenRoute = ({ component: Component, ...rest }) => (
  <Route {...rest} component={(props) => <Component {...props} />} />
)

const App = () => (
  <>
    <Switch>
      <DefaultRoute path="/" exact name="HomePage" component={HomePage} />
      <DefaultRoute path="/backup" exact name="Backup" component={BackupPage} />
      <DefaultRoute path="/dashboard" exact name="Dashboard" component={DashboardPage} />
      <DefaultRoute path="/datamanagement" exact name="DataManagement" component={DataManagementPage} />
      <DefaultRoute path="/datamanagement/csvexport" exact name="CsvDataExport" component={CsvDataExportPage} />
      <DefaultRoute path="/datamanagement/backup" exact name="BackupDataExport" component={BackupDataExportPage} />
      <DefaultRoute
        path="/datamanagement/backupimport"
        exact
        name="BackupDataImport"
        component={BackupDataImportPage}
      />
      <DefaultRoute path="/datamanagement/csvimport" exact name="CsvDataImport" component={CsvDataImportPage} />
      <DefaultRoute path="/datamanagement/:id" name="RecordDetails" component={OldClientRecordEditPage} />
      <DefaultRoute path="/datamanagement_new/:id" name="RecordDetailsNew" component={RecordEditPage} />
      <DefaultRoute path="/datacleansing" exact name="DataCleansing" component={DataCleansingPage} />
      <DefaultRoute path="/map" exact name="Map" component={MapPage} />
      <DefaultRoute path="/restore" exact name="Restore" component={RestorePage} />
      <DefaultRoute path="/saiku" exact name="Saiku" component={SaikuPage} />
      <DefaultRoute path="/surveydesigner" exact name="SurveysList" component={SurveysListPage} />
      <DefaultRoute path="/surveydesigner/new" exact name="NewSurvey" component={NewSurveyPage} />
      <DefaultRoute path="/surveydesigner/surveyimport" exact name="SurveyImport" component={SurveyImportPage} />
      <DefaultRoute path="/surveydesigner/:id" exact name="SurveyEdit" component={SurveyEditPage} />
      <DefaultRoute path="/surveydesigner/export/:id" exact name="SurveyExport" component={SurveyExportPage} />
      <DefaultRoute path="/surveydesigner/clone/:surveyName" exact name="SurveyClone" component={SurveyClonePage} />
      <DefaultRoute path="/users" exact name="Users" component={UsersPage} />
      <DefaultRoute path="/users/changepassword" exact name="ChangePassword" component={PasswordChangePage} />
      <DefaultRoute path="/usergroups" exact name="User Groups" component={UserGroupsPage} />
      <DefaultRoute path="/usergroups/:id" name="User Group" component={UserGroupDetailsPage} />

      <FullScreenRoute
        path="/surveypreview/:id"
        exact
        name="SurveyDataEntryPreview"
        component={SurveyDataEntryPreviewPage}
      />
    </Switch>
    <CurrentJobMonitorDialog />
    <AppWebSocket />
  </>
)

export default App
