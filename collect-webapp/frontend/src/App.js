import './App.scss'

import React, { useEffect } from 'react'
import { Route, Routes } from 'react-router-dom'
import { connect, useDispatch } from 'react-redux'

import Header from 'common/components/Header'
import Sidebar from 'common/components/Sidebar'
import CurrentJobMonitorDialog from 'common/containers/CurrentJobMonitorDialog'
import SystemErrorDialog from 'common/containers/SystemErrorDialog'

import EventQueue from 'model/event/EventQueue'

import HomePage from 'scenes/HomePage'
import DashboardPage from 'scenes/DashboardPage'
import DataCleansingPage from 'scenes/DataCleansingPage'
import MapPage from 'scenes/MapPage'
import SaikuPage from 'scenes/SaikuPage'

import BackupDataExportPage from 'datamanagement/pages/BackupDataExportPage'
import BackupDataImportPage from 'datamanagement/pages/BackupDataImportPage'
import CsvDataExportPage from 'datamanagement/pages/CsvDataExportPage'
import CsvDataImportPage from 'datamanagement/pages/CsvDataImportPage'
import RecordEditPage from 'datamanagement/pages/RecordEditPage'
import DataManagementPage from 'datamanagement/pages/DataManagementPage'
import { RandomGridGenerationPage } from 'datamanagement/pages/RandomGridGenerationPage'
import BackupPage from 'backuprestore/pages/BackupPage'
import RestorePage from 'backuprestore/pages/RestorePage'
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

import NavigationController from 'NavigationController'

const DefaultLayoutRoutes = () => (
  <div className="app">
    <Header />
    <div className="app-body">
      <Sidebar />
      <main className="main">
        <div className="main-content-wrapper">
          <Routes>
            <Route path="/" name="HomePage" element={<HomePage />} />
            <Route path="/backup" name="Backup" element={<BackupPage />} />
            <Route path="/dashboard" name="Dashboard" element={<DashboardPage />} />
            <Route path="/datamanagement" name="DataManagement" element={<DataManagementPage />} />
            <Route path="/datamanagement/csvexport" name="CsvDataExport" element={<CsvDataExportPage />} />
            <Route path="/datamanagement/backup" name="BackupDataExport" element={<BackupDataExportPage />} />
            <Route path="/datamanagement/backupimport" name="BackupDataImport" element={<BackupDataImportPage />} />
            <Route path="/datamanagement/csvimport" name="CsvDataImport" element={<CsvDataImportPage />} />
            <Route
              path="/datamanagement/randomgrid"
              name="RandomGridGeneration"
              element={<RandomGridGenerationPage />}
            />
            <Route path="/datamanagement/:id" name="RecordEdit" element={<RecordEditPage />} />
            <Route path="/datacleansing" name="DataCleansing" element={<DataCleansingPage />} />
            <Route path="/map" name="Map" element={<MapPage />} />
            <Route path="/restore" name="Restore" element={<RestorePage />} />
            <Route path="/saiku" name="Saiku" element={<SaikuPage />} />
            <Route path="/surveydesigner" name="SurveysList" element={<SurveysListPage />} />
            <Route path="/surveydesigner/new" name="NewSurvey" element={<NewSurveyPage />} />
            <Route path="/surveydesigner/surveyimport" name="SurveyImport" element={<SurveyImportPage />} />
            <Route path="/surveydesigner/:id" name="SurveyEdit" element={<SurveyEditPage />} />
            <Route path="/surveydesigner/export/:id" name="SurveyExport" element={<SurveyExportPage />} />
            <Route path="/surveydesigner/clone/:surveyName" name="SurveyClone" element={<SurveyClonePage />} />
            <Route path="/users" name="Users" element={<UsersPage />} />
            <Route path="/users/changepassword" name="ChangePassword" element={<PasswordChangePage />} />
            <Route path="/usergroups" name="User Groups" element={<UserGroupsPage />} />
            <Route path="/usergroups/:id" name="User Group" element={<UserGroupDetailsPage />} />
            <Route path="*" name="Not found" element={<>Not found</>} />
          </Routes>
        </div>
      </main>
    </div>
  </div>
)

const App = (props) => {
  const { systemErrorShown, systemErrorMessage, systemErrorStackTrace } = props

  const dispatch = useDispatch()

  useEffect(() => {
    // init EventQueue
    EventQueue.dispatch = dispatch
  }, [])

  return (
    <>
      <Routes>
        <Route path="surveypreview/:id" name="SurveyDataEntryPreview" element={<SurveyDataEntryPreviewPage />} />
        <Route path="record_fullscreen/:id" name="RecordEditFullScreen" element={<RecordEditPage />} />
        <Route path="*" element={<DefaultLayoutRoutes />} />
      </Routes>

      <NavigationController />

      <CurrentJobMonitorDialog />

      {systemErrorShown && (
        <SystemErrorDialog message={systemErrorMessage} details={systemErrorStackTrace} width={800} />
      )}
    </>
  )
}

const mapStateToProps = (state) => {
  const { show: systemErrorShown, message: systemErrorMessage, stackTrace: systemErrorStackTrace } = state.systemError

  return {
    systemErrorShown,
    systemErrorMessage,
    systemErrorStackTrace,
  }
}

export default connect(mapStateToProps)(App)
