import ServiceFactory from 'services/ServiceFactory';
import User from 'model/User';
import RouterUtils from 'utils/RouterUtils';

export const REQUEST_CURRENT_USER = 'REQUEST_CURRENT_USER';
export const RECEIVE_CURRENT_USER = 'RECEIVE_CURRENT_USER';
export const REQUEST_LOGOUT = 'REQUEST_LOGOUT';
export const LOGOUT_PERFORMED = 'LOGOUT_PERFORMED';
export const SESSION_EXPIRED = 'SESSION_EXPIRED';

function requestCurrentUser() {
	return {
		type: REQUEST_CURRENT_USER
	}
}

export function fetchCurrentUser() {
	return function (dispatch) {
		dispatch(requestCurrentUser())
		ServiceFactory.sessionService.fetchCurrentUser().then(json => {
			dispatch(receiveCurrentUser(json));
		})
	}
}

function receiveCurrentUser(json) {
	return {
	    type: RECEIVE_CURRENT_USER,
	    user: new User(json)
	}
}

function requestLogout() {
	return {
		type: REQUEST_LOGOUT
	}
}

export function logout() {
    return function (dispatch) {
        dispatch(requestLogout())
        ServiceFactory.sessionService.invalidate()
            .then(dispatch(logoutPerformed()))
            .then(r => RouterUtils.navigateToLoginPage(true))
    }
}

function logoutPerformed() {
    return {
        type: LOGOUT_PERFORMED
    }
}

export function sessionExpired() {
    return {
        type: SESSION_EXPIRED
    }
}
