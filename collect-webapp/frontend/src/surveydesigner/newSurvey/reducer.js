import {
    NEW_SURVEY_CREATED, SURVEY_CREATION_ERROR, RESET_NEW_SURVEY_FORM
} from './actions'


const SURVEYS_BASE_STATE = {
    surveyCreationErrors: [],
    newSurveyCreated: false,
    newSurveySummary: null
}

export default function (
    state = SURVEYS_BASE_STATE,
    action
) {
    switch (action.type) {
        case NEW_SURVEY_CREATED:
            return Object.assign({}, state, {
                newSurveyCreated: true,
                newSurveySummary: action.newSurveySummary,
                surveyCreationErrors: []
            })
        case SURVEY_CREATION_ERROR:
            return Object.assign({}, state, {
                surveyCreationErrors: action.errors
            })
        case RESET_NEW_SURVEY_FORM:
            return Object.assign({}, state, {
                newSurveyCreated: false,
                newSurveySummary: null,
                surveyCreationErrors: []
            })
        default:
            return state
    }
}