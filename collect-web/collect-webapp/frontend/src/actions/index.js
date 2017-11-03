import Constants from 'utils/Constants'
import ServiceFactory from 'services/ServiceFactory'
import { Survey } from 'model/Survey';
import User from 'model/User';

export const REQUEST_APPLICATION_INFO = 'REQUEST_APPLICATION_INFO'
export const RECEIVE_APPLICATION_INFO = 'RECEIVE_APPLICATION_INFO'

export const LOG_IN_PENDING = 'LOG_IN_PENDING'
export const LOG_IN_SUCCESS = 'LOG_IN_SUCCESS'
export const LOG_IN_FAILED = 'LOG_IN_FAILED'

export const REQUEST_CURRENT_USER = 'REQUEST_CURRENT_USER'
export const RECEIVE_CURRENT_USER = 'RECEIVE_CURRENT_USER'

export const REQUEST_SURVEY_SUMMARIES = 'REQUEST_SURVEY_SUMMARIES'
export const RECEIVE_SURVEY_SUMMARIES = 'RECEIVE_SURVEY_SUMMARIES'
export const INVALIDATE_SURVEY_SUMMARIES = 'INVALIDATE_SURVEY_SUMMARIES'

export const SELECT_PREFERRED_SURVEY = 'SELECT_PREFERRED_SURVEY'
export const REQUEST_FULL_PREFERRED_SURVEY = 'REQUEST_FULL_PREFERRED_SURVEY'
export const RECEIVE_FULL_PREFERRED_SURVEY = 'RECEIVE_FULL_PREFERRED_SURVEY'
export const INVALIDATE_PREFERRED_SURVEY = 'INVALIDATE_PREFERRED_SURVEY'

export const REQUEST_USER_GROUPS = 'REQUEST_USER_GROUPS'
export const RECEIVE_USER_GROUPS = 'RECEIVE_USER_GROUPS'
export const RECEIVE_USER_GROUP = 'RECEIVE_USER_GROUP'
export const INVALIDATE_USER_GROUPS = 'INVALIDATE_USER_GROUPS'

export const START_JOB_MONITOR = 'START_JOB_MONITOR'
export const CLOSE_JOB_MONITOR = 'CLOSE_JOB_MONITOR'
export const REQUEST_JOB = 'REQUEST_JOB'
export const RECEIVE_JOB = 'RECEIVE_JOB'
export const JOB_CANCELED = 'JOB_CANCELED'

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

//LOGIN
function loginPending() {
	return {
		type: LOG_IN_PENDING
	}
}

function loginSuccess() {  
	return {
		type: LOG_IN_SUCCESS
	}
}

function loginFailed() {  
	return {
		type: LOG_IN_FAILED
	}
}

export function logInUser(credentials) {  
	return function(dispatch) {
		dispatch(loginPending());

		function handleErrors(response) {
			if (!response.ok) {
				throw Error(response.statusText);
			}
			return response;
		}
		
		ServiceFactory.userService.login(credentials)
			.then(handleErrors)
			.then(response => {
				if (!response.ok || response.url.indexOf("login_error") > 0) {
					dispatch(loginFailed());
				} else {
					dispatch(loginSuccess());
					dispatch(fetchCurrentUser());
				}
			})
	};
}

function requestCurrentUser() {
	return {
		type: REQUEST_CURRENT_USER
	}
}

export function fetchCurrentUser() {
	return function (dispatch) {
		dispatch(requestCurrentUser())
		ServiceFactory.sessionService.fetchCurrentUser().then(json => {
			dispatch(receiveCurrentUser(json));
			dispatch(fetchSurveySummaries(json));
		})
	}
}

function receiveCurrentUser(json) {
	return {
	    type: RECEIVE_CURRENT_USER,
	    user: new User(json)
	}
}

//SURVEY SUMMARIES
function requestSurveySummaries() {
	return {
		type: REQUEST_SURVEY_SUMMARIES
	}
}

function receiveSurveySummaries(json) {
	return {
	    type: RECEIVE_SURVEY_SUMMARIES,
	    summaries: json.map(s => s), //TODO map into Survey object
	    receivedAt: Date.now()
	}
}

export function fetchSurveySummaries() {
	return function (dispatch) {
		dispatch(requestSurveySummaries())
		ServiceFactory.surveyService.fetchAllSummaries().then(json => 
			dispatch(receiveSurveySummaries(json))
		)
	}
}

export function invalidateSurveySummaries(summaries) {
	return {
		type: INVALIDATE_SURVEY_SUMMARIES,
		summaries
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

//JOB
function requestJob() {
	return {
		type: REQUEST_JOB
	}
}

export class jobMonitorConfiguration {
	jobId
	title = 'Processing'
	okButtonLabel = 'Ok'
	handleOkButtonClick
	handleCancelButtonClick
	handleJobCompleted
	handleJobFailed
}

export function startJobMonitor(jobMonitorConfiguration) {
	return {
		type: START_JOB_MONITOR,
		jobMonitorConfiguration: jobMonitorConfiguration
	}
}

export function fetchJob(jobId) {
	return function (dispatch) {
		dispatch(requestJob())
		ServiceFactory.jobService.fetch(jobId).then(job => dispatch(receiveJob(job)))
	}
}

export function receiveJob(job) {
	return {
	    type: RECEIVE_JOB,
	    job: job,
	    receivedAt: Date.now()
	}
}

export function cancelJob(jobId) {
	return function (dispatch) {
		ServiceFactory.jobService.cancel(jobId).then(() => dispatch(jobCanceled(jobId)))
	}
}

function jobCanceled(jobId) {
	return {
		type: JOB_CANCELED,
		jobId: jobId,
		receivedAt: Date.now()
	}
}

export function closeJobMonitor() {
	return {
		type: CLOSE_JOB_MONITOR
	}
}