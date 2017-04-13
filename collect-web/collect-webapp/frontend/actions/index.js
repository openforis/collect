export const INVALIDATE_SURVEY_SUMMARIES = 'INVALIDATE_SURVEY_SUMMARIES'
export const REQUEST_SURVEY_SUMMARIES = 'REQUEST_SURVEY_SUMMARIES'
export const RECEIVE_SURVEY_SUMMARIES = 'RECEIVE_SURVEY_SUMMARIES'
export const SELECT_PREFERRED_SURVEY = 'SELECT_PREFERRED_SURVEY'
export const REQUEST_RECORDS = 'REQUEST_RECORDS'
export const RECEIVE_RECORDS = 'RECEIVE_RECORDS'
export const INVALIDATE_SURVEY = 'INVALIDATE_SURVEY'

export const requestSurveySummaries = () => ({
  type: REQUEST_SURVEY_SUMMARIES
})

export const receiveSurveySummaries = json => ({
  type: RECEIVE_SURVEY_SUMMARIES,
  summaries: json.data.map(summary => summary.data),
  receivedAt: Date.now()
})

export const fetchSurveySummaries = reddit => dispatch => {
  dispatch(requestRecords(survey))
  var url = 'http://localhost:8380/collect/survey/summaries.json';
  return fetch(url)
    .then(response => response.json())
    .then(json => dispatch(receiveSurveySummaries(json)))
}

export const invalidateSurveySummaries = reddit => ({
  type: INVALIDATE_SURVEY_SUMMARIES,
  reddit
})
	
export const selectPreferredSurvey = survey => ({
  type: SELECT_PREFERRED_SURVEY,
  survey
})

export const invalidateSurvey = reddit => ({
  type: INVALIDATE_SURVEY,
  reddit
})

export const requestRecords = survey => ({
  type: REQUEST_RECORDS,
  survey
})

export const receiveRecords = (survey, json) => ({
  type: RECEIVE_RECORDS,
  survey,
  records: json.data.children.map(child => child.data),
  receivedAt: Date.now()
})

const fetchRecords = reddit => dispatch => {
  dispatch(requestRecords(survey))
  var url = '';
  return fetch(url)
    .then(response => response.json())
    .then(json => dispatch(receiveRecords(survey, json)))
}

const shouldFetchRecords = (state, reddit) => {
  const records = state.recordsBySurvey[survey]
  if (!records) {
    return true
  }
  if (records.isFetching) {
    return false
  }
  return records.didInvalidate
}

export const fetchRecordsIfNeeded = survey => (dispatch, getState) => {
  if (shouldFetchRecords(getState(), survey)) {
    return dispatch(fetchRecords(survey))
  }
}