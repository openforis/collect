import ServiceFactory from 'services/ServiceFactory'
import { Survey } from 'model/Survey'
import BrowserUtils from 'utils/BrowserUtils'
import Arrays from 'utils/Arrays'

export const REQUEST_APPLICATION_INFO = 'REQUEST_APPLICATION_INFO'
export const RECEIVE_APPLICATION_INFO = 'RECEIVE_APPLICATION_INFO'

export const REQUEST_FULL_ACTIVE_SURVEY = 'REQUEST_FULL_ACTIVE_SURVEY'
export const RECEIVE_FULL_ACTIVE_SURVEY = 'RECEIVE_FULL_ACTIVE_SURVEY'
export const INVALIDATE_ACTIVE_SURVEY = 'INVALIDATE_ACTIVE_SURVEY'

export const SELECT_ACTIVE_SURVEY_LANGUAGE = 'SELECT_ACTIVE_SURVEY_LANGUAGE'

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
		info
	}
}

//ACTIVE SURVEY
export function selectActiveSurvey(activeSurveySummary) {
	let surveyId = activeSurveySummary.id;
	return function (dispatch) {
		dispatch(requestFullActiveSurvey(surveyId))
		ServiceFactory.surveyService.fetchById(surveyId).then(json => {
			const browserLangCode = BrowserUtils.determineBrowserLanguageCode()
			const language = Arrays.contains(json.languages, browserLangCode) ? browserLangCode : json.languages[0]
			dispatch(receiveFullActiveSurvey(json, language))
		})
	}
}

export function receiveFullActiveSurvey(json, language) {
	return {
		type: RECEIVE_FULL_ACTIVE_SURVEY,
		survey: new Survey(json),
		language,
		receivedAt: Date.now()
	}
}

export function requestFullActiveSurvey(surveyId) {
	return {
		type: REQUEST_FULL_ACTIVE_SURVEY,
		surveyId: surveyId,
		receivedAt: Date.now()
	}
}

export function invalidateActiveSurvey(activeSurvey) {
	return {
		type: INVALIDATE_ACTIVE_SURVEY,
		activeSurvey
	}
}

export function selectActiveSurveyLanguage(language) {
	return {
		type: SELECT_ACTIVE_SURVEY_LANGUAGE,
		language
	}
}
