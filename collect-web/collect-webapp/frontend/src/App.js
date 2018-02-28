import React, { Component } from 'react';
import { Switch, Route } from 'react-router-dom'

import Header from 'components/Header';
import Sidebar from 'components/Sidebar';
import CurrentJobMonitorDialog from 'containers/CurrentJobMonitorDialog';
import HomePage from 'scenes/HomePage'
import BackupDataExportPage from 'scenes/datamanagement/BackupDataExportPage'
import BackupDataImportPage from 'scenes/datamanagement/BackupDataImportPage'
import CsvDataExportPage from 'scenes/datamanagement/CsvDataExportPage'
import CsvDataImportPage from 'scenes/datamanagement/CsvDataImportPage'
import OldClientRecordEditPage from 'scenes/datamanagement/OldClientRecordEditPage'
import BackupPage from 'scenes/backuprestore/BackupPage'
import DashboardPage from 'scenes/DashboardPage'
import DataCleansingPage from 'scenes/DataCleansingPage'
import DataManagementPage from 'scenes/datamanagement/DataManagementPage'
import MapPage from 'scenes/MapPage'
import RestorePage from 'scenes/backuprestore/RestorePage'
import SaikuPage from 'scenes/SaikuPage'
import SurveyDesignerPage from 'scenes/SurveyDesignerPage'
import SurveyClonePage from 'scenes/surveydesigner/SurveyClonePage'
import SurveyEditPage from 'scenes/surveydesigner/SurveyEditPage'
import SurveysListPage from 'scenes/surveydesigner/SurveysListPage'
import NewSurveyPage from 'scenes/surveydesigner/NewSurveyPage';
import SurveyExportPage from 'scenes/surveydesigner/SurveyExportPage';
import SurveyImportPage from 'scenes/surveydesigner/SurveyImportPage';
import UsersPage from 'scenes/users/UsersPage'
import UserGroupsPage from 'scenes/users/UserGroupsPage'
import UserGroupDetailsPage from 'scenes/users/UserGroupDetailsPage'
import PasswordChangePage from 'scenes/users/PasswordChangePage'

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
        </div>
    );
  }
}

export default App;
