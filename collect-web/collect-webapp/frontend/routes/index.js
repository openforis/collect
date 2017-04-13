/**
 * React Starter Kit (https://www.reactstarterkit.com/)
 *
 * Copyright © 2014-2016 Kriasoft, LLC. All rights reserved.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE.txt file in the root directory of this source tree.
 */

import React from 'react';
import { createStore, applyMiddleware } from 'redux'
import { Provider } from 'react-redux'
import thunk from 'redux-thunk'
import { createLogger } from 'redux-logger'
import reducer from '../reducers'
import App from '../components/app';

// Child routes
import home from './home';
import login from './login';
import blank from './dashboard_pages/blank';
import error from './error';
import DataCleansing from './data_cleansing';
import DataManagement from './data_management';

import Header from '../components/header';

const middleware = [ thunk ]
if (process.env.NODE_ENV !== 'production') {
  middleware.push(createLogger())
}

const store = createStore(
  reducer,
  applyMiddleware(...middleware)
)

export default [

  {
    path: '/login',
    children: [
      login,
    ],
    async action({ next, render, context }) {
      const component = await next();
      if (component === undefined) return component;
      return render(
        <Provider store={store}>
          <App context={context}>{component}</App>
        </Provider>
      );
    },
  },


  {
    path: '/',

  // keep in mind, routes are evaluated in order
    children: [
      home,
      DataManagement,
      DataCleansing,
      // place new routes before...
      error,
    ],

    async action({ next, render, context }) {
      // console.log('inside dashboard');
      const component = await next();
      // console.log('inside dasdboard component', component);
      if (component === undefined) return component;
      return render(
        <Provider store={store}>
	        <div>
	          <Header />
	          <div id="page-wrapper" className="page-wrapper">
	            <App context={context}>{component}</App>
	          </div>
	        </div>
        </Provider>
      );
    },
  },
  {
    path: '/error',
    children: [
      error,
    ],
    async action({ next, render, context }) {
      // console.log('inside error');
      const component = await next();
      // console.log('inside error with component', component);
      if (component === undefined) return component;
      return render(
        <Provider store={store}>
          <App context={context}>{component}</App>
        </Provider>
      );
    },
  },
];
