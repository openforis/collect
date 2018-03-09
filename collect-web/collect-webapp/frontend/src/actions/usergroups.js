import ServiceFactory from 'services/ServiceFactory'

export const REQUEST_USER_GROUPS = 'REQUEST_USER_GROUPS'
export const RECEIVE_USER_GROUPS = 'RECEIVE_USER_GROUPS'
export const RECEIVE_USER_GROUP = 'RECEIVE_USER_GROUP'
export const DELETE_USER_GROUPS = 'DELETE_USER_GROUPS'
export const USER_GROUPS_DELETED = 'USER_GROUPS_DELETED'
export const INVALIDATE_USER_GROUPS = 'INVALIDATE_USER_GROUPS'

function requestUserGroups() {
	return {
		type: REQUEST_USER_GROUPS
	}
}

function receiveUserGroups(json) {
	return {
	    type: RECEIVE_USER_GROUPS,
	    userGroups: json.map(g => {
			if (g.parentId != null) {
				g.parent = json.find(g2 => g2.id === g.parentId);
			}
			return g;
		}), //TODO map into UserGroup object
	    receivedAt: Date.now()
	}
}

export function receiveUserGroup(json) {
	return {
	    type: RECEIVE_USER_GROUP,
	    userGroup: json,
	    receivedAt: Date.now()
	}
}

export function fetchUserGroups() {
	return function (dispatch) {
		dispatch(requestUserGroups())
		ServiceFactory.userGroupService.fetchAllAvailableGroups().then(json => dispatch(receiveUserGroups(json)))
	}
}

export function invalidateUserGroups(userGroups) {
	return {
		type: INVALIDATE_USER_GROUPS,
		userGroups
	}
}

export function deleteUserGroups(loggedUserId, userGroupIds) {
	return function (dispatch) {
		ServiceFactory.userGroupService.deleteUserGroups(loggedUserId, userGroupIds).then(json => 
			dispatch(userGroupsDeleted(userGroupIds))
		)
	}
}

function userGroupsDeleted(itemIds) {
	return {
		type: USER_GROUPS_DELETED,
		itemIds: itemIds
	}
}