import Numbers from 'utils/Numbers'

const KEYS = {
  activeSurvey: 'activeSurvey',
}

export const setActiveSurveyId = (surveyId) => {
  localStorage.setItem(KEYS.activeSurvey, String(surveyId))
}

export const getActiveSurveyId = () => Numbers.toNumber(localStorage.getItem(KEYS.activeSurvey))

export const deleteActiveSurveyId = () => localStorage.removeItem(KEYS.activeSurvey)
