import User from 'model/User'
import ServiceFactory from 'services/ServiceFactory'

export const REQUEST_USERS = 'REQUEST_USERS'
export const RECEIVE_USERS = 'RECEIVE_USERS'
export const INVALIDATE_USERS = 'INVALIDATE_USERS'
export const DELETE_USER = 'DELETE_USER'
export const DELETE_USERS = 'DELETE_USERS'
export const USERS_DELETED = 'USERS_DELETED'
export const UPDATE_USER = 'UPDATE_USER'
export const RECEIVE_USER = 'RECEIVE_USER'

function requestUsers() {
	return {
		type: REQUEST_USERS
	}
}

function receiveUsers(json) {
	return {
	    type: RECEIVE_USERS,
		users: json.map(u => new User(u)), 
	    receivedAt: Date.now()
	}
}

export function receiveUser(json) {
	return {
	    type: RECEIVE_USER,
	    user: new User(json),
	    receivedAt: Date.now()
	}
}

export function fetchUsers() {
	return function (dispatch) {
		dispatch(requestUsers())
		ServiceFactory.userService.fetchUsers().then(json => dispatch(receiveUsers(json)))
	}
}

export function deleteUser(userId) {
	deleteUsers([userId])
}

export function deleteUsers(loggedUserId, userIds) {
	return function (dispatch) {
		ServiceFactory.userService.deleteUsers(loggedUserId, userIds).then(json => 
			dispatch(usersDeleted(userIds))
		)
	}
}

function usersDeleted(userIds) {
	return {
		type: USERS_DELETED,
		userIds: userIds
	}
}

export function invalidateUsers(users) {
	return {
		type: INVALIDATE_USERS,
		users
	}
}

