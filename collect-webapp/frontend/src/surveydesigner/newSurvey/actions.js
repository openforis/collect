import Forms from 'common/components/Forms';

import ServiceFactory from 'services/ServiceFactory';

export const REQUEST_CREATE_NEW_SURVEY = 'REQUEST_CREATE_NEW_SURVEY'
export const NEW_SURVEY_CREATED = 'NEW_SURVEY_CREATED'
export const SURVEY_CREATION_ERROR = 'SURVEY_CREATION_ERROR'
export const RESET_NEW_SURVEY_FORM = 'RESET_NEW_SURVEY_FORM'

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

export function newSurveyCreated(newSurveySummary) {
    return {
        type: NEW_SURVEY_CREATED,
        newSurveySummary
    }
}

export function resetNewSurveyForm() {
    return {
        type: RESET_NEW_SURVEY_FORM
    }
}

