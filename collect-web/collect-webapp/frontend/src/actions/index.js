import fetch from 'isomorphic-fetch'

export const INVALIDATE_SURVEY_SUMMARIES = 'INVALIDATE_SURVEY_SUMMARIES'
export const REQUEST_SURVEY_SUMMARIES = 'REQUEST_SURVEY_SUMMARIES'
export const RECEIVE_SURVEY_SUMMARIES = 'RECEIVE_SURVEY_SUMMARIES'
export const SELECT_PREFERRED_SURVEY = 'SELECT_PREFERRED_SURVEY'
export const REQUEST_FULL_PREFERRED_SURVEY = 'REQUEST_FULL_PREFERRED_SURVEY'
export const RECEIVE_FULL_PREFERRED_SURVEY = 'RECEIVE_FULL_PREFERRED_SURVEY'
export const INVALIDATE_PREFERRED_SURVEY = 'INVALIDATE_PREFERRED_SURVEY'

let CONTEXT_PATH = 'http://localhost:8480/collect/'
	
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
		var url = CONTEXT_PATH + 'survey/summaries.json';
		return fetch(url)
		    .then(response => response.json(),
	    		error => console.log('An error occured.', error))
		    .then(json => dispatch(receiveSurveySummaries(json)))
	}
}

export function invalidateSurveySummaries(summaries) {
	return {
		type: INVALIDATE_SURVEY_SUMMARIES,
		summaries
	}
}
	
export function selectPreferredSurvey(preferredSurveySummary) {
	let surveyId = preferredSurveySummary.id;
	return function (dispatch) {
		dispatch(requestFullPreferredSurvey(surveyId))
		var url = CONTEXT_PATH + 'survey/' + surveyId + '.json';
		return fetch(url)
		    .then(response => response.json(),
	    		error => console.log('An error occured.', error))
		    .then(json => dispatch(receiveFullPreferredSurvey(json)))
	}
}


export function receiveFullPreferredSurvey(json) {
	return {
		type: RECEIVE_FULL_PREFERRED_SURVEY,
		survey: json,
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
