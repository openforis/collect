import ServiceFactory from 'services/ServiceFactory'
import { Survey } from 'model/Survey';

export const REQUEST_APPLICATION_INFO = 'REQUEST_APPLICATION_INFO'
export const RECEIVE_APPLICATION_INFO = 'RECEIVE_APPLICATION_INFO'

export const SELECT_PREFERRED_SURVEY = 'SELECT_PREFERRED_SURVEY'
export const REQUEST_FULL_PREFERRED_SURVEY = 'REQUEST_FULL_PREFERRED_SURVEY'
export const RECEIVE_FULL_PREFERRED_SURVEY = 'RECEIVE_FULL_PREFERRED_SURVEY'
export const INVALIDATE_PREFERRED_SURVEY = 'INVALIDATE_PREFERRED_SURVEY'

export const REQUEST_USER_GROUPS = 'REQUEST_USER_GROUPS'
export const RECEIVE_USER_GROUPS = 'RECEIVE_USER_GROUPS'
export const RECEIVE_USER_GROUP = 'RECEIVE_USER_GROUP'
export const INVALIDATE_USER_GROUPS = 'INVALIDATE_USER_GROUPS'

export const RECORD_DELETED = 'RECORD_DELETED'
export const RECORDS_DELETED = 'RECORDS_DELETED'

//APPLICATION INFO
function requestApplicationInfo() {
	return {
		type: REQUEST_APPLICATION_INFO
	}
}

export function fetchApplicationInfo() {
	return function (dispatch) {
		dispatch(requestApplicationInfo())
		ServiceFactory.applicationInfoService.fetchInfo().then(json => {
			dispatch(receiveApplicationInfo(json));
		})
	}
}

function receiveApplicationInfo(info) {
	return {
		type: RECEIVE_APPLICATION_INFO,
		info: info
	}
}

//PREFERRED SURVEY
export function selectPreferredSurvey(preferredSurveySummary) {
	let surveyId = preferredSurveySummary.id;
	return function (dispatch) {
		dispatch(requestFullPreferredSurvey(surveyId))
		ServiceFactory.surveyService.fetchById(surveyId).then(json => 
			dispatch(receiveFullPreferredSurvey(json))
		)
	}
}


export function receiveFullPreferredSurvey(json) {
	return {
		type: RECEIVE_FULL_PREFERRED_SURVEY,
		survey: new Survey(json),
		receivedAt: Date.now()
	}
}

export function requestFullPreferredSurvey(surveyId) {
	return {
		type: REQUEST_FULL_PREFERRED_SURVEY,
		surveyId,
		receivedAt: Date.now()
	}
}

export function invalidatePreferredSurvey(preferredSurvey) {
	return {
		type: INVALIDATE_PREFERRED_SURVEY,
		preferredSurvey
	}
}

//USER GROUPS
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

//RECORDS
export function recordDeleted(record) {
	return {
		type: RECORD_DELETED,
		record: record
	}
}

export function recordsDeleted(records) {
	return {
		type: RECORDS_DELETED,
		records: records
	}
}
