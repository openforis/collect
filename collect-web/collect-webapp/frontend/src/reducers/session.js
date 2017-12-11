import {
    REQUEST_CURRENT_USER, RECEIVE_CURRENT_USER, REQUEST_LOGOUT, LOGOUT_PERFORMED, SESSION_EXPIRED
} from 'actions/session'

function session(
    state = {
        initialized: false,
        isFetching: false,
        didInvalidate: false,
        loggedUser: null,
        sessionExpired: false,
        loggingOut: false,
        loggedOut: false
    },
    action
) {
    switch (action.type) {
        case REQUEST_CURRENT_USER:
            return Object.assign({}, state, {
                isFetching: true,
                didInvalidate: false
            })
        case RECEIVE_CURRENT_USER:
            return Object.assign({}, state, {
                initialized: true,
                isFetching: false,
                didInvalidate: false,
                loggedUser: action.user
            })
        case REQUEST_LOGOUT:
            return Object.assign({}, state, {
                loggingOut: true
            })
        case LOGOUT_PERFORMED:
            return Object.assign({}, state, {
                loggingOut: false,
                loggedOut: true
            })
        case SESSION_EXPIRED:
            return Object.assign({}, state, {
                sessionExpired: true
            })
        default:
            return state
    }
}

export default session