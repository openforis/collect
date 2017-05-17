export const INVALIDATE_SURVEY_SUMMARIES = 'INVALIDATE_SURVEY_SUMMARIES'
export const REQUEST_SURVEY_SUMMARIES = 'REQUEST_SURVEY_SUMMARIES'
export const RECEIVE_SURVEY_SUMMARIES = 'RECEIVE_SURVEY_SUMMARIES'
export const REQUEST_FULL_PREFERRED_SURVEY = 'REQUEST_FULL_PREFERRED_SURVEY'
export const RECEIVE_FULL_PREFERRED_SURVEY = 'RECEIVE_FULL_PREFERRED_SURVEY'
export const INVALIDATE_PREFERRED_SURVEY = 'INVALIDATE_PREFERRED_SURVEY'

export const requestSurveySummaries = () => ({
	type: REQUEST_SURVEY_SUMMARIES
})

export const fetchSurveySummaries = () => dispatch => {
  dispatch(requestSurveySummaries())
  var url = 'http://localhost:8380/collect/survey/summaries.json';
  return fetch(url)
    .then(response => response.json())
    .then(json => dispatch(receiveSurveySummaries(json)))
}

export const receiveSurveySummaries = json => ({
  type: RECEIVE_SURVEY_SUMMARIES,
  summaries: json,
  receivedAt: Date.now()
})

export const invalidateSurveySummaries = () => ({
  type: INVALIDATE_SURVEY_SUMMARIES
})
	
export const requestFullPreferredSurvey = survey => ({
  type: RECEIVE_FULL_PREFERRED_SURVEY
})

export const fetchFullPreferredSurvey = (surveyId) => dispatch => {
  dispatch(requestSurveySummaries())
  var url = 'http://localhost:8380/collect/survey/' + surveyId + '.json';
  return fetch(url)
    .then(response => response.json())
    .then(json => dispatch(receiveFullPreferredSurvey(json)))
}

export const receiveFullPreferredSurvey = json => ({
  type: RECEIVE_FULL_PREFERRED_SURVEY,
  survey: json,
  receivedAt: Date.now()
})

export const invalidatePreferredSurvey = () => ({
  type: INVALIDATE_PREFERRED_SURVEY
})
