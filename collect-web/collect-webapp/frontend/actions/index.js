export const INVALIDATE_SURVEY_SUMMARIES = 'INVALIDATE_SURVEY_SUMMARIES'
export const REQUEST_SURVEY_SUMMARIES = 'REQUEST_SURVEY_SUMMARIES'
export const RECEIVE_SURVEY_SUMMARIES = 'RECEIVE_SURVEY_SUMMARIES'
export const SELECT_PREFERRED_SURVEY = 'SELECT_PREFERRED_SURVEY'
export const INVALIDATE_PREFERRED_SURVEY = 'INVALIDATE_PREFERRED_SURVEY'

export const requestSurveySummaries = () => ({
	type: REQUEST_SURVEY_SUMMARIES
})

export const receiveSurveySummaries = json => ({
  type: RECEIVE_SURVEY_SUMMARIES,
  summaries: json,
  receivedAt: Date.now()
})

export const fetchSurveySummaries = () => dispatch => {
  dispatch(requestSurveySummaries())
  var url = 'http://localhost:8380/collect/survey/summaries.json';
  return fetch(url)
    .then(response => response.json())
    .then(json => dispatch(receiveSurveySummaries(json)))
}

export const invalidateSurveySummaries = () => ({
  type: INVALIDATE_SURVEY_SUMMARIES
})
	
export const selectPreferredSurvey = survey => ({
  type: SELECT_PREFERRED_SURVEY,
  survey
})

export const invalidatePreferredSurvey = () => ({
  type: INVALIDATE_PREFERRED_SURVEY
})
