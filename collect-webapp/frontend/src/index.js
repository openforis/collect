import React from 'react'
import ReactDOM from 'react-dom'
import { BrowserRouter, Route, Routes } from 'react-router-dom'
import thunkMiddleware from 'redux-thunk'
import { createStore, applyMiddleware, compose } from 'redux'
import { Provider } from 'react-redux'

import 'bootstrap/dist/css/bootstrap.css'

// Containers
import App from 'App'

// Pages
import Signin from 'scenes/Pages/Signin'
import Register from 'scenes/Pages/Register'
import Page404 from 'scenes/Pages/Page404'
import Page500 from 'scenes/Pages/Page500'

import rootReducer from 'reducers'
import Startup from 'common/Startup/Startup'
import SessionTimeoutVerifier from 'common/containers/SessionTimeoutVerifier'
import Labels from 'utils/Labels'

const composeEnhancers = window.__REDUX_DEVTOOLS_EXTENSION_COMPOSE__ || compose

const store = createStore(
  rootReducer,
  composeEnhancers(
    applyMiddleware(
      thunkMiddleware // lets us dispatch() functions,
    )
  )
)

Labels.initialize(() => {
  ReactDOM.render(
    <Provider store={store}>
      <SessionTimeoutVerifier>
        <Startup>
          <BrowserRouter>
            <Routes>
              <Route path="/signin" name="Signin Page" element={<Signin />} />
              <Route path="/register" name="Register Page" element={<Register />} />
              <Route path="/404" name="Page 404" element={<Page404 />} />
              <Route path="/500" name="Page 500" element={<Page500 />} />
              <Route path="*" name="Home" element={<App />} />
            </Routes>
          </BrowserRouter>
        </Startup>
      </SessionTimeoutVerifier>
    </Provider>,
    document.getElementById('root')
  )
})
