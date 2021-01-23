import ServiceFactory from 'services/ServiceFactory'
import User from 'model/User'
import RouterUtils from 'utils/RouterUtils'

export const REQUEST_CURRENT_USER = 'REQUEST_CURRENT_USER'
export const RECEIVE_CURRENT_USER = 'RECEIVE_CURRENT_USER'
export const REQUEST_LOGOUT = 'REQUEST_LOGOUT'
export const LOGOUT_PERFORMED = 'LOGOUT_PERFORMED'
export const SESSION_EXPIRED = 'SESSION_EXPIRED'

const requestCurrentUser = () => ({
  type: REQUEST_CURRENT_USER,
})

export const fetchCurrentUser = () => (dispatch) => {
  dispatch(requestCurrentUser())
  ServiceFactory.sessionService.fetchCurrentUser().then((json) => {
    dispatch(receiveCurrentUser(json))
  })
}

const receiveCurrentUser = (json) => ({
  type: RECEIVE_CURRENT_USER,
  user: new User(json),
})

const requestLogout = () => ({
  type: REQUEST_LOGOUT,
})

export const logout = () => (dispatch) => {
  dispatch(requestLogout())
  ServiceFactory.sessionService
    .invalidate()
    .then(dispatch(logoutPerformed()))
    .then(() => RouterUtils.navigateToLoginPage(true))
}

const logoutPerformed = () => ({
  type: LOGOUT_PERFORMED,
})

export const sessionExpired = () => ({
  type: SESSION_EXPIRED,
})
