import {
  REQUEST_USERS, RECEIVE_USERS, INVALIDATE_USERS
} from '../actions'

function users(
  state = {
	  isFetching: false,
	  didInvalidate: false,
	  users: []
	},
	action
) {
  switch (action.type) {
    case INVALIDATE_USERS:
      return Object.assign({}, state, {
        didInvalidate: true
      })
    case REQUEST_USERS:
      return Object.assign({}, state, {
	        isFetching: true,
	        didInvalidate: false
        })
    case RECEIVE_USERS:
      return Object.assign({}, state, {
		    isFetching: false,
		    didInvalidate: false,
		    users: action.users,
		    lastUpdated: action.receivedAt
		  })
    default:
      return state
  }
}

export default users