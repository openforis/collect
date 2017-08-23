import {
  REQUEST_USER_GROUPS, RECEIVE_USER_GROUPS, INVALIDATE_USER_GROUPS
} from '../actions'

function userGroups(
  state = {
	  isFetching: false,
	  didInvalidate: false,
	  userGroups: []
	},
	action
) {
  switch (action.type) {
    case INVALIDATE_USER_GROUPS:
      return Object.assign({}, state, {
        didInvalidate: true
      })
    case REQUEST_USER_GROUPS:
      return Object.assign({}, state, {
	        isFetching: true,
	        didInvalidate: false
        })
    case RECEIVE_USER_GROUPS:
      return Object.assign({}, state, {
		    isFetching: false,
		    didInvalidate: false,
		    userGroups: action.userGroups,
		    lastUpdated: action.receivedAt
		  })
    default:
      return state
  }
}

export default userGroups