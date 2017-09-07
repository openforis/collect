import createReactClass from 'create-react-class';

class Constants {

    static BASE_URL = Constants.determineBaseURL();
    static API_BASE_URL = Constants.BASE_URL + "api/";

    static determineBaseURL() {
        if (Constants.isDevReact()) {
            return "http://127.0.0.1:8080/collect/";
        } else {
            let l = window.location;
            return l.origin + l.pathname;
        }
    }

    static isDevReact() {
        try {
          createReactClass();
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