import update from 'react-addons-update';
import Arrays from 'utils/Arrays'

import {
  REQUEST_USER_GROUPS, RECEIVE_USER_GROUPS, RECEIVE_USER_GROUP, INVALIDATE_USER_GROUPS
} from '../actions'

function userGroups(
  state = {
    initialized: false,
	  isFetching: false,
	  didInvalidate: false,
	  items: []
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
      action.userGroups.forEach(ug => {
          //adjust parent group reference
          ug.parent = ug.parentId ? action.userGroups.find(ug2 => ug2.id === ug.parentId) : null
          ug.children = ug.childrenGroupIds.map(id => action.userGroups.find(ug2 => ug2.id === id))
      })
      
      return Object.assign({}, state, {
        initialized: true,
		    isFetching: false,
		    didInvalidate: false,
		    items: action.userGroups,
		    lastUpdated: action.receivedAt
      })
    case RECEIVE_USER_GROUP:
      const newUserGroup = action.userGroup
      const userGroups = state.items
      const userGroupIdx = userGroups.findIndex(u => u.id === newUserGroup.id)
      const oldUserGroup = userGroups[userGroupIdx]
      const oldUserGroupParentId = oldUserGroup.parentId
      if (oldUserGroupParentId !== null) {
        const oldParentGroup = userGroups.find(ug => ug.id === oldUserGroupParentId)
        oldParentGroup.childrenGroupIds = Arrays.removeItem(oldParentGroup.childrenGroupIds, newUserGroup.id)
        oldParentGroup.children = Arrays.removeItem(oldParentGroup.children, oldUserGroup)
      }
      if (newUserGroup.parentId === null) {
        newUserGroup.parent = null
      } else {
        const parentGroup = userGroups.find(ug => ug.id === newUserGroup.parentId)
        newUserGroup.parent = parentGroup
        if (parentGroup.childrenGroupIds.indexOf(newUserGroup.id) < 0) {
          parentGroup.childrenGroupIds.push(newUserGroup.id)
          parentGroup.children.push(newUserGroup)
        }
      }
        
      var newUserGroups = update(userGroups, {
        $splice: [[userGroupIdx, 1, newUserGroup]]
      });
      return Object.assign({}, state, {
        items: newUserGroups,
        lastUpdated: action.receivedAt
      })
    default:
      return state
  }
}

export default userGroups