import React from 'react';
import ReactDOM from 'react-dom';
import { HashRouter, Route, Switch } from 'react-router-dom'
import thunkMiddleware from 'redux-thunk'
import { createStore, applyMiddleware } from 'redux'
import { Provider } from 'react-redux'

import 'bootstrap/dist/css/bootstrap.css';

// Containers
import App from 'App'

// Pages
import Signin from 'scenes/Pages/Signin/'
import Register from 'scenes/Pages/Register/'
import Page404 from 'scenes/Pages/Page404/'
import Page500 from 'scenes/Pages/Page500/'

import rootReducer from 'reducers'
import Startup from 'common/containers/Startup'
import SessionTimeoutVerifier from 'common/containers/SessionTimeoutVerifier'
import Labels from 'utils/Labels'

const store = createStore(
  rootReducer,
  applyMiddleware(
    thunkMiddleware, // lets us dispatch() functions
  )
)

Labels.initialize(() => {
  ReactDOM.render((
    <Provider store={store}>
      <SessionTimeoutVerifier>
        <Startup>
          <HashRouter>
            <Switch>
              <Route exact path="/signin" name="Signin Page" component={Signin} />
              <Route exact path="/register" name="Register Page" component={Register} />
              <Route exact path="/404" name="Page 404" component={Page404} />
              <Route exact path="/500" name="Page 500" component={Page500} />
              <Route path="/" name="Home" component={App} />
            </Switch>
          </HashRouter>
        </Startup>
      </SessionTimeoutVerifier>
    </Provider>
  ), document.getElementById('root'))
})


