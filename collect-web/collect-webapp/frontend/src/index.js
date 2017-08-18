import React from 'react';
import ReactDOM from 'react-dom';
import { HashRouter, Route, Switch } from 'react-router-dom'
import { createBrowserHistory } from 'history';
import thunkMiddleware from 'redux-thunk'
import { createLogger } from 'redux-logger'
import { createStore, applyMiddleware } from 'redux'
import { Provider } from 'react-redux'

// Containers
import App from './containers/App/'

// Views
import Signin from './views/Pages/Signin/'
import Register from './views/Pages/Register/'
import Page404 from './views/Pages/Page404/'
import Page500 from './views/Pages/Page500/'

import rootReducer from './reducers'
import Startup from './Startup'

const loggerMiddleware = createLogger()

const store = createStore(
  rootReducer,
  applyMiddleware(
    thunkMiddleware, // lets us dispatch() functions
    loggerMiddleware // neat middleware that logs actions
  )
)
const history = createBrowserHistory();

ReactDOM.render((
  <Provider store={store}>
    <Startup>
      <HashRouter history={history}>
        <Switch>
          <Route exact path="/signin" name="Signin Page" component={Signin}/>
          <Route exact path="/register" name="Register Page" component={Register}/>
          <Route exact path="/404" name="Page 404" component={Page404}/>
          <Route exact path="/500" name="Page 500" component={Page500}/>
          <Route path="/" name="Home" component={App} />
        </Switch>
      </HashRouter>
    </Startup>
  </Provider>
), document.getElementById('root'))
