import ServiceFactory from 'services/ServiceFactory';
import Forms from 'components/Forms'

export const REQUEST_SURVEY_SUMMARIES = 'REQUEST_SURVEY_SUMMARIES'
export const RECEIVE_SURVEY_SUMMARIES = 'RECEIVE_SURVEY_SUMMARIES'
export const INVALIDATE_SURVEY_SUMMARIES = 'INVALIDATE_SURVEY_SUMMARIES'
export const CHANGE_SURVEY_USER_GROUP = 'CHANGE_SURVEY_USER_GROUP'
export const REQUEST_SURVEY_USER_GROUP_CHANGE = 'REQUEST_SURVEY_USER_GROUP_CHANGE'
export const SURVEY_USER_GROUP_CHANGED = 'SURVEY_USER_GROUP_CHANGED'
export const REQUEST_CREATE_NEW_SURVEY = 'REQUEST_CREATE_NEW_SURVEY'
export const NEW_SURVEY_CREATED = 'NEW_SURVEY_CREATED'
export const SURVEY_CREATION_ERROR = "SURVEY_CREATION_ERROR"

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

function surveyUserGroupChanged(surveyId, newUserGroupId) {
    return {
        type: SURVEY_USER_GROUP_CHANGED,
        surveyId: surveyId,
        newUserGroupId: newUserGroupId,
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

function requestUserGroupChange() {
    return {
        type: REQUEST_SURVEY_USER_GROUP_CHANGE
    }
}

export function changeUserGroup(surveyId, newUserGroupId, loggedUserId) {
    return function (dispatch) {
        dispatch(requestUserGroupChange())
        ServiceFactory.surveyService.changeUserGroup(surveyId, newUserGroupId, loggedUserId).then(summary =>
            dispatch(surveyUserGroupChanged(surveyId, newUserGroupId))
        )
    }
}

export function invalidateSurveySummaries(summaries) {
    return {
        type: INVALIDATE_SURVEY_SUMMARIES,
        summaries
    }
}

function requestCreateNewSurvey(name, template, defaultLanguageCode, userGroupId) {
    return {
        type: REQUEST_CREATE_NEW_SURVEY,
        name: name,
        template: template,
        defaultLanguageCode: defaultLanguageCode,
        userGroupId: userGroupId
    }
}

function surveyCreationError(errors) {
    return {
        type: SURVEY_CREATION_ERROR,
        errors: errors
    }
}

export function createNewSurvey(name, template, defaultLanguageCode, userGroupId) {
    return function (dispatch) {
        dispatch(requestCreateNewSurvey(name, template, defaultLanguageCode, userGroupId))
        return ServiceFactory.surveyService.createNewSurvey(name, template, defaultLanguageCode, userGroupId).then(res => {
            if (res.statusError) {
                Forms.handleValidationResponse(res)
                dispatch(surveyCreationError(res.objects.errors))
            } else {
                dispatch(newSurveyCreated(res.object))
            }
        })
    }
}

function newSurveyCreated(summary) {
    return {
        type: NEW_SURVEY_CREATED,
        summary: summary
    }
}