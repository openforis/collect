import ServiceFactory from 'services/ServiceFactory';

export const REQUEST_SURVEY_SUMMARIES = 'REQUEST_SURVEY_SUMMARIES'
export const RECEIVE_SURVEY_SUMMARIES = 'RECEIVE_SURVEY_SUMMARIES'
export const INVALIDATE_SURVEY_SUMMARIES = 'INVALIDATE_SURVEY_SUMMARIES'

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
