import Dialogs from 'common/components/Dialogs';

import ServiceFactory from 'services/ServiceFactory';

import L from 'utils/Labels';

export const REQUEST_SURVEY_SUMMARIES = 'REQUEST_SURVEY_SUMMARIES'
export const RECEIVE_SURVEY_SUMMARIES = 'RECEIVE_SURVEY_SUMMARIES'
export const INVALIDATE_SURVEY_SUMMARIES = 'INVALIDATE_SURVEY_SUMMARIES'
export const CHANGE_SURVEY_USER_GROUP = 'CHANGE_SURVEY_USER_GROUP'
export const REQUEST_SURVEY_USER_GROUP_CHANGE = 'REQUEST_SURVEY_USER_GROUP_CHANGE'
export const SHOW_SURVEY_VALIDATION = 'SHOW_SURVEY_VALIDATION'
export const HIDE_SURVEY_VALIDATION = 'HIDE_SURVEY_VALIDATION'

export const SURVEY_CREATED = 'SURVEY_CREATED'
export const SURVEY_UPDATED = 'SURVEY_UPDATED'
export const SURVEY_DELETED = 'SURVEY_DELETED'

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

const showSurveyValidation = (survey, validationResult) => ({
    type: SHOW_SURVEY_VALIDATION,
    survey,
    validationResult
})

export const hideSurveyValidation = () => ({
    type: HIDE_SURVEY_VALIDATION
})

export function fetchSurveySummaries() {
    return function (dispatch) {
        dispatch(requestSurveySummaries())
        try {
            ServiceFactory.surveyService.fetchAllSummaries().then(json =>
                dispatch(receiveSurveySummaries(json))
            )
        } catch (e) { }
    }
}

export const publishSurvey = (survey, ignoreWarnings = false, onComplete = null) =>
    dispatch => {
        dispatch(hideSurveyValidation())
        const loadingDialog = Dialogs.showLoadingDialog()

        ServiceFactory.surveyService.publish(survey.id, ignoreWarnings)
            .then(response => {
                if (response.validationResult) {
                    dispatch(showSurveyValidation(survey, response.validationResult))
                } else {
                    Dialogs.alert(L.l('survey.publish.successDialog.title'),
                    L.l('survey.publish.successDialog.message', survey.name))
                    //survey update managed by WebSocket
                    //dispatchSurveyUpdated(dispatch, s)
                }
                if (onComplete)
                    onComplete()
            })
            .catch(() => {
                Dialogs.alert(L.l('survey.publish.error.title'), L.l('common.systemError.message'))
            })
            .finally(() => loadingDialog.close())
    }

export const unpublishSurvey = survey =>
    dispatch => {
        const surveyId = survey.temporary ? survey.publishedId : survey.id
        ServiceFactory.surveyService.unpublish(surveyId).then(s => {
            Dialogs.alert(L.l('survey.unpublish.successDialog.title'),
                L.l('survey.unpublish.successDialog.message', survey.name))
            //survey update managed by WebSocket
            //dispatchSurveyUpdated(dispatch, s)
        })

    }

function requestUserGroupChange() {
    return {
        type: REQUEST_SURVEY_USER_GROUP_CHANGE
    }
}

export function changeUserGroup(surveySummary, newUserGroupId, loggedUserId) {
    return function (dispatch) {
        dispatch(requestUserGroupChange())
        ServiceFactory.surveyService.changeUserGroup(surveySummary.name, newUserGroupId, loggedUserId)
            .then(s => {
                //survey update managed by WebSocket
                //dispatchSurveyUpdated(dispatch, s)
            })
    }
}

export function invalidateSurveySummaries(summaries) {
    return {
        type: INVALIDATE_SURVEY_SUMMARIES,
        summaries
    }
}

export function deleteSurvey(survey) {
    return function (dispatch) {
        ServiceFactory.surveyService.delete(survey.id).then(r => {
            Dialogs.alert(L.l('survey.delete.success.title'), L.l('survey.delete.success.message', survey.name))
            dispatch(surveyDeleted(survey))
        })
    }
}

export function surveyCreated(surveySummary) {
    return {
        type: SURVEY_CREATED,
        surveySummary
    }
}

export function surveyUpdated(surveySummary) {
    return {
        type: SURVEY_UPDATED,
        surveySummary
    }
}

export function surveyDeleted(surveySummary) {
    return {
        type: SURVEY_DELETED,
        surveySummary
    }
}