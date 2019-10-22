import Forms from 'common/components/Forms';

import ServiceFactory from 'services/ServiceFactory';

export const REQUEST_CREATE_NEW_SURVEY = 'REQUEST_CREATE_NEW_SURVEY'
export const NEW_SURVEY_CREATED = 'NEW_SURVEY_CREATED'
export const SURVEY_CREATION_ERROR = 'SURVEY_CREATION_ERROR'
export const RESET_NEW_SURVEY_FORM = 'RESET_NEW_SURVEY_FORM'

const requestCreateNewSurvey = surveyFormObj => ({
    type: REQUEST_CREATE_NEW_SURVEY,
    ...surveyFormObj
})

const surveyCreationError = errors => ({
    type: SURVEY_CREATION_ERROR,
    errors: errors
})

export const createNewSurvey = surveyFormObj => dispatch => {
    dispatch(requestCreateNewSurvey(surveyFormObj))
    return ServiceFactory.surveyService.createNewSurvey(surveyFormObj).then(res => {
        if (res.statusError) {
            Forms.handleValidationResponse(res)
            dispatch(surveyCreationError(res.objects.errors))
        } else {
            dispatch(newSurveyCreated(res.object))
        }
    })
}

export const newSurveyCreated = newSurveySummary => ({
    type: NEW_SURVEY_CREATED,
    newSurveySummary
})

export const resetNewSurveyForm = () => ({
    type: RESET_NEW_SURVEY_FORM
})

