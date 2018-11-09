import React, { Component } from 'react'
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
import NewSurveyPage from 'surveydesigner/newSurvey/pages/NewSurveyPage'
import SurveyExportPage from 'surveydesigner/pages/SurveyExportPage'
import SurveyImportPage from 'surveydesigner/surveyImport/pages/SurveyImportPage'
import UsersPage from 'security/pages/UsersPage'
import UserGroupsPage from 'security/pages/UserGroupsPage'
import UserGroupDetailsPage from 'security/pages/UserGroupDetailsPage'
import PasswordChangePage from 'security/pages/PasswordChangePage'

import AppWebSocket from 'ws/appWebSocket'

class App extends Component {

  render() {
    
    return (
        <div className="app">
          <Header />
          <div className="app-body">
            <Sidebar {...this.props}/>
            <main className="main">
              <div className="main-content-wrapper">
                <Switch>
                  <Route path="/" exact name="HomePage" component={HomePage}/>
                  <Route path="/backup" exact name="Backup" component={BackupPage}/>
                  <Route path="/dashboard" exact name="Dashboard" component={DashboardPage}/>
                  <Route path="/datamanagement" exact name="DataManagement" component={DataManagementPage}/>
                  <Route path="/datamanagement/csvexport" exact name="CsvDataExport" component={CsvDataExportPage}/>
                  <Route path="/datamanagement/backup" exact name="BackupDataExport" component={BackupDataExportPage}/>
                  <Route path="/datamanagement/backupimport" exact name="BackupDataImport" component={BackupDataImportPage}/>
                  <Route path="/datamanagement/csvimport" exact name="CsvDataImport" component={CsvDataImportPage}/>
                  <Route path="/datamanagement/:id" name="RecordDetails" component={OldClientRecordEditPage}/>
                  <Route path="/datacleansing" exact name="DataCleansing" component={DataCleansingPage}/>
                  <Route path="/map" exact name="Map" component={MapPage}/>
                  <Route path="/restore" exact name="Restore" component={RestorePage}/>
                  <Route path="/saiku" exact name="Saiku" component={SaikuPage}/>
                  <Route path="/surveydesigner" exact name="SurveysList" component={SurveysListPage}/>
                  <Route path="/surveydesigner/new" exact name="NewSurvey" component={NewSurveyPage}/>
                  <Route path="/surveydesigner/surveyimport" exact name="SurveyImport" component={SurveyImportPage}/>
                  <Route path="/surveydesigner/:id" exact name="SurveyEdit" component={SurveyEditPage}/>
                  <Route path="/surveydesigner/export/:id" exact name="SurveyExport" component={SurveyExportPage}/>
                  <Route path="/surveydesigner/clone/:surveyName" exact name="SurveyClone" component={SurveyClonePage}/>
                  <Route path="/users" exact name="Users" component={UsersPage}/>
                  <Route path="/users/changepassword" exact name="ChangePassword" component={PasswordChangePage}/>
                  <Route path="/usergroups" exact name="User Groups" component={UserGroupsPage}/>
                  <Route path="/usergroups/:id" name="User Group" component={UserGroupDetailsPage}/>
                </Switch>
              </div>
            </main>
          </div>
          <CurrentJobMonitorDialog />
          <AppWebSocket />
        </div>
    );
  }
}

export default App;
