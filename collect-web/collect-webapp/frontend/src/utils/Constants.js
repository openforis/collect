export default class Constants {

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
        return typeof JSON3 !== 'undefined' //TODO improve it
      };
}
