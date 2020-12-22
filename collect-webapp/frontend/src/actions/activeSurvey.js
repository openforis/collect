import ServiceFactory from 'services/ServiceFactory'
import { Survey } from 'model/Survey'
import BrowserUtils from 'utils/BrowserUtils'
import Arrays from 'utils/Arrays'
import { ActiveSurveyLocalStorage } from 'localStorage'

export const ACTIVE_SURVEY_REQUESTED = 'ACTIVE_SURVEY_REQUESTED'
export const ACTIVE_SURVEY_FETCHED = 'ACTIVE_SURVEY_FETCHED'
export const SELECT_ACTIVE_SURVEY_LANGUAGE = 'SELECT_ACTIVE_SURVEY_LANGUAGE'
export const ACTIVE_SURVEY_CLEARED = 'ACTIVE_SURVEY_CLEARED'

const activeSurveyReceived = (json, language) => ({
  type: ACTIVE_SURVEY_FETCHED,
  survey: new Survey(json),
  language,
  receivedAt: Date.now(),
})

const requestFullActiveSurvey = (surveyId) => ({
  type: ACTIVE_SURVEY_REQUESTED,
  surveyId: surveyId,
  receivedAt: Date.now(),
})

export const selectActiveSurvey = (surveyId) => (dispatch) => {
  dispatch(requestFullActiveSurvey(surveyId))

  ActiveSurveyLocalStorage.setActiveSurveyId(surveyId)

  ServiceFactory.surveyService.fetchById(surveyId).then((json) => {
    const browserLangCode = BrowserUtils.determineBrowserLanguageCode()
    const language = Arrays.contains(json.languages, browserLangCode) ? browserLangCode : json.languages[0]
    dispatch(activeSurveyReceived(json, language))
  })
}

export const selectActiveSurveyLanguage = (language) => ({
  type: SELECT_ACTIVE_SURVEY_LANGUAGE,
  language,
})

export const clearActiveSurvey = () => (dispatch) => {
  ActiveSurveyLocalStorage.removeActiveSurveyId()

  dispatch({
    type: ACTIVE_SURVEY_CLEARED,
  })
}
