import React, { Component } from 'react';
import { Switch, Route, Redirect } from 'react-router-dom'

import Header from 'components/Header';
import Sidebar from 'components/Sidebar';
import Breadcrumb from 'components/Breadcrumb';
import Aside from 'components/Aside';
import Footer from 'components/Footer';

import HomePage from 'containers/HomePage'
import DashboardPage from 'containers/DashboardPage'
import DataCleansingPage from 'containers/DataCleansingPage'
import MapPage from 'containers/MapPage'
import SurveyDesignerPage from 'containers/SurveyDesignerPage'
import UsersPage from 'containers/users/UsersPage'
import UserGroupsPage from 'containers/users/UserGroupsPage'
import UserGroupDetailsPage from 'containers/users/UserGroupDetailsPage'
import DataManagementPage from 'containers/datamanagement/DataManagementPage'
import DataExportPage from 'containers/datamanagement/DataExportPage'
import DataImportPage from 'containers/datamanagement/DataImportPage'
import OldClientRecordEditPage from 'containers/datamanagement/OldClientRecordEditPage'

class App extends Component {
  render() {
    return (
        <div className="app">
          <Header />
          <div className="app-body">
            <Sidebar {...this.props}/>
            <main className="main">
              <Breadcrumb />
              <div className="container-fluid">
                <Switch>
                  <Route path="/" exact name="HomePage" component={HomePage}/>
                  <Route path="/dashboard" exact name="Dashboard" component={DashboardPage}/>
                  <Route path="/datamanagement" exact name="DataManagement" component={DataManagementPage}/>
                  <Route path="/datamanagement/export" exact name="DataExport" component={DataExportPage}/>
                  <Route path="/datamanagement/import" exact name="DataImport" component={DataImportPage}/>
                  <Route path="/datamanagement/:id" name="RecordDetails" component={OldClientRecordEditPage}/>
                  <Route path="/datacleansing" exact name="DataCleansing" component={DataCleansingPage}/>
                  <Route path="/map" exact name="Map" component={MapPage}/>
                  <Route path="/surveydesigner" exact name="Survey Designer" component={SurveyDesignerPage}/>
                  <Route path="/users" exact name="Users" component={UsersPage}/>
                  <Route path="/usergroups" exact name="User Groups" component={UserGroupsPage}/>
                  <Route path="/usergroups/:id" name="User Group" component={UserGroupDetailsPage}/>
                </Switch>
              </div>
            </main>
            <Aside />
          </div>
          <Footer />
        </div>
    );
  }
}

export default App;
