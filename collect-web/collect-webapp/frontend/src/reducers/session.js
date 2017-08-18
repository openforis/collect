import {
    REQUEST_CURRENT_USER, RECEIVE_CURRENT_USER
    } from '../actions'


function session(
    state = {
        isFetching: false,
        didInvalidate: false,
        loggedUser: null
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
	        isFetching: true,
            didInvalidate: false,
            user: action.user
        })
    default:
      return state
  }
}

export default session