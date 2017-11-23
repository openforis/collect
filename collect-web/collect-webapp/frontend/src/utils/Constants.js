import Objects from 'utils/Objects'

export default class Constants {

    static APP_VERSION = Objects.defaultIfNull(process.env.REACT_APP_COLLECT_PROJECT_VERSION, "3.14.0-SNAPSHOT")
    static BASE_URL = Constants.determineBaseURL();
    static BASE_ASSETS_URL = Constants.determineBaseAssetsURL();
    static API_BASE_URL = Constants.BASE_URL + "api/";

    static determineBaseURL() {
        if (Constants.isDevReact()) {
            return "http://127.0.0.1:8080/collect/";
        } else {
            let l = window.location;
            return l.origin + l.pathname;
        }
    }

    static determineBaseAssetsURL() {
        let l = window.location;
        return l.origin + l.pathname + 'assets/';
    }

    static isDevReact() {
        return typeof JSON3 !== 'undefined' //TODO improve it
    }

}
