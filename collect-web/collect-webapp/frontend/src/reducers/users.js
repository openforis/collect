import update from 'react-addons-update';
import Arrays from 'utils/Arrays'

import {
  REQUEST_USERS, RECEIVE_USERS, RECEIVE_USER, USER_DELETED, USERS_DELETED, INVALIDATE_USERS
} from '../actions/users'

function users(
  state = {
    initialized: false,
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
        initialized: true,
		    isFetching: false,
		    didInvalidate: false,
		    users: action.users,
		    lastUpdated: action.receivedAt
      })
    case USER_DELETED:
      const oldUser = state.users.find(u => u.id === action.userId)
      if (oldUser) {
        let newUsers = Arrays.removeItem(state.users, oldUser)
        return Object.assign({}, state, {
          users: newUsers,
          lastUpdated: action.receivedAt
        })
      } else {
        return state
      }
    case USERS_DELETED: {
      const deletedUserIds = action.userIds
      const deletedUsers = deletedUserIds.map(id => state.users.find(u => u.id === id))
      const newUsers = Arrays.removeItems(state.users, deletedUsers)
      return Object.assign({}, state, {
        users: newUsers,
        lastUpdated: action.receivedAt
      })
    }
    case RECEIVE_USER:
      const userIdx = state.users.findIndex(u => u.id === action.user.id)
      let newUsers
      if (userIdx < 0) {
        newUsers = update(state.users, {
          $push: [ action.user]
        });
        newUsers.sort((a, b) => {
          return a.username.localeCompare(b.username)
        })
      } else {
        newUsers = update(state.users, {
          $splice: [[userIdx, 1, action.user]]
        });
      }
      return Object.assign({}, state, {
        users: newUsers,
        lastUpdated: action.receivedAt
      })
    default:
      return state
  }
}

export default users