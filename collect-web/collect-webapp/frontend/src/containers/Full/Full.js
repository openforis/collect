import React, { Component } from 'react';
import { Switch, Route, Redirect } from 'react-router-dom'
import { Provider } from 'react-redux'
import thunkMiddleware from 'redux-thunk'
import { createLogger } from 'redux-logger'
import { createStore, applyMiddleware } from 'redux'

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

import { fetchSurveySummaries } from '../../actions'
import rootReducer from '../../reducers'

const loggerMiddleware = createLogger()

const store = createStore(
  rootReducer,
  applyMiddleware(
    thunkMiddleware, // lets us dispatch() functions
    loggerMiddleware // neat middleware that logs actions
  )
)

store.dispatch(fetchSurveySummaries())

class Full extends Component {
  render() {
    return (
      <Provider store={store}>
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
                  <Redirect from="/" to="/dashboard"/>
                </Switch>
              </div>
            </main>
            <Aside />
          </div>
          <Footer />
        </div>
      </Provider>
    );
  }
}

export default Full;
