import ServiceFactory from 'services/ServiceFactory'
import { Survey } from 'model/Survey'
import BrowserUtils from 'utils/BrowserUtils'
import Arrays from 'utils/Arrays'

export const REQUEST_APPLICATION_INFO = 'REQUEST_APPLICATION_INFO'
export const RECEIVE_APPLICATION_INFO = 'RECEIVE_APPLICATION_INFO'

export const REQUEST_FULL_PREFERRED_SURVEY = 'REQUEST_FULL_PREFERRED_SURVEY'
export const RECEIVE_FULL_PREFERRED_SURVEY = 'RECEIVE_FULL_PREFERRED_SURVEY'
export const INVALIDATE_PREFERRED_SURVEY = 'INVALIDATE_PREFERRED_SURVEY'

export const SELECT_PREFERRED_SURVEY_LANGUAGE = 'SELECT_PREFERRED_SURVEY_LANGUAGE'

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
		ServiceFactory.surveyService.fetchById(surveyId).then(json => {
			dispatch(receiveFullPreferredSurvey(json))
			const browserLangCode = BrowserUtils.determineBrowserLanguageCode()
			const language = Arrays.contains(json.languages, browserLangCode) ? browserLangCode : json.languages[0]
			dispatch(selectPreferredSurveyLanguage(language))
		})
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
		surveyId: surveyId,
		receivedAt: Date.now()
	}
}

export function invalidatePreferredSurvey(preferredSurvey) {
	return {
		type: INVALIDATE_PREFERRED_SURVEY,
		preferredSurvey: preferredSurvey
	}
}

export function selectPreferredSurveyLanguage(language) {
	return {
		type: SELECT_PREFERRED_SURVEY_LANGUAGE,
		language: language
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
