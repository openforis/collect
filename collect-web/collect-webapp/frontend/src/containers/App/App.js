import React, { Component } from 'react';
import { Switch, Route, Redirect } from 'react-router-dom'

import Header from '../../components/Header/';
import Sidebar from '../../components/Sidebar/';
import Breadcrumb from '../../components/Breadcrumb/';
import Aside from '../../components/Aside/';
import Footer from '../../components/Footer/';

import Dashboard from '../../views/Dashboard/'
import DataCleansing from '../../views/DataCleansing/'
import DataManagement from '../../views/DataManagement/'
import Map from '../../views/Map/'
import SurveyDesigner from '../../views/SurveyDesigner/'
import Users from '../../views/Users/'
import UserGroups from '../../views/UserGroups/'

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
                  <Route path="/dashboard" name="Dashboard" component={Dashboard}/>
                  <Route path="/datamanagement" name="DataManagement" component={DataManagement}/>
                  <Route path="/datacleansing" name="DataCleansing" component={DataCleansing}/>
                  <Route path="/map" name="Map" component={Map}/>
                  <Route path="/surveydesigner" name="Survey Designer" component={SurveyDesigner}/>
                  <Route path="/users" name="Users" component={Users}/>
                  <Route path="/usergroups" name="User Groups" component={UserGroups}/>
                  <Redirect from="/" to="/dashboard"/>
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
