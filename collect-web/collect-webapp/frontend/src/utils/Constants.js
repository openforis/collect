import React, { Component, PropTypes } from 'react'

class Constants {

    static SERVICES_URL = Constants.determineServicesURL();

    static determineServicesURL() {
        if (Constants.isDevReact()) {
            return "http://127.0.0.1:8480/collect/";
        } else {
            return this.props.location.URL;
        }
    }

    static isDevReact() {
        try {
          React.createClass({});
        } catch(e) {
          if (e.message.indexOf('render') >= 0) {
            return true;  // A nice, specific error message
          } else {
            return false;  // A generic error message
          }
        }
        return false;  // should never happen, but play it safe.
      };
}

export default Constants;