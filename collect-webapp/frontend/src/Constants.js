import Objects from 'utils/Objects'

const DEV_VERSION = '4.0.1-SNAPSHOT'
const DEV_CONTEXT_PATH = '/collect'
const DEV_BASE_URL = `http://127.0.0.1:8080${DEV_CONTEXT_PATH}/`

const getWindowLocationContextPath = () => {
  return window.location.pathname.split('/')[1]
}

const getWindowLocationBaseUrl = () => {
  const l = window.location
  const context = getWindowLocationContextPath()
  return `${l.origin}/${context}/`
}

export default class Constants {
  static APP_VERSION = Objects.defaultIfNull(process.env.REACT_APP_COLLECT_PROJECT_VERSION, DEV_VERSION)
  static BASE_PATH = Constants.determineBasePath()
  static BASE_URL = Constants.determineBaseURL()
  static BASE_ASSETS_URL = Constants.determineBaseAssetsURL()
  static API_BASE_URL = Constants.BASE_URL + 'api/'

  static determineBasePath() {
    return Constants.isDevEnvironment() ? DEV_CONTEXT_PATH : getWindowLocationContextPath()
  }

  static determineBaseURL() {
    return Constants.isDevEnvironment() ? DEV_BASE_URL : getWindowLocationBaseUrl()
  }

  static determineBaseAssetsURL() {
    return `${
      Constants.isDevEnvironment() ? `${window.location.origin}${DEV_CONTEXT_PATH}/` : getWindowLocationBaseUrl()
    }assets/`
  }

  static isDevEnvironment() {
    const env = process.env.NODE_ENV
    return !env || env === 'development'
  }
}
