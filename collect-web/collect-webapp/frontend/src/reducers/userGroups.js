import update from 'react-addons-update';

import {
  REQUEST_USER_GROUPS, RECEIVE_USER_GROUPS, RECEIVE_USER_GROUP, INVALIDATE_USER_GROUPS
} from '../actions'

function userGroups(
  state = {
    initialized: false,
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
        initialized: true,
		    isFetching: false,
		    didInvalidate: false,
		    userGroups: action.userGroups,
		    lastUpdated: action.receivedAt
      })
    case RECEIVE_USER_GROUP:
      const newUserGroup = action.userGroup
      const userGroups = state.userGroups
      const userGroupIdx = userGroups.findIndex(u => u.id === newUserGroup.id)
      if (newUserGroup.parentId != null) {
        const parentGroup = userGroups.find(ug => ug.id === newUserGroup.parentId)
        newUserGroup.parent = parentGroup
        if (parentGroup.chindrenGroupIds.indexOf(newUserGroup.id) < 0) {
          parentGroup.childrenGroupIds.push(newUserGroup.id)
        }
      } else {
        newUserGroup.parent = null
      }
      var newUserGroups = update(userGroups, {
        $splice: [[userGroupIdx, 1, newUserGroup]]
      });
      return Object.assign({}, state, {
        userGroups: newUserGroups,
        lastUpdated: action.receivedAt
      })
    default:
      return state
  }
}

export default userGroups