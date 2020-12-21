import ServiceFactory from 'services/ServiceFactory'
import { Survey } from 'model/Survey'
import BrowserUtils from 'utils/BrowserUtils'
import Arrays from 'utils/Arrays'
import { ActiveSurveyLocalStorage } from 'localStorage'

export const REQUEST_FULL_ACTIVE_SURVEY = 'REQUEST_FULL_ACTIVE_SURVEY'
export const RECEIVE_FULL_ACTIVE_SURVEY = 'RECEIVE_FULL_ACTIVE_SURVEY'
export const INVALIDATE_ACTIVE_SURVEY = 'INVALIDATE_ACTIVE_SURVEY'

export const SELECT_ACTIVE_SURVEY_LANGUAGE = 'SELECT_ACTIVE_SURVEY_LANGUAGE'

const receiveFullActiveSurvey = (json, language) => ({
  type: RECEIVE_FULL_ACTIVE_SURVEY,
  survey: new Survey(json),
  language,
  receivedAt: Date.now(),
})

const requestFullActiveSurvey = (surveyId) => ({
  type: REQUEST_FULL_ACTIVE_SURVEY,
  surveyId: surveyId,
  receivedAt: Date.now(),
})

export const selectActiveSurvey = (surveyId) => (dispatch) => {
  dispatch(requestFullActiveSurvey(surveyId))

  ActiveSurveyLocalStorage.setActiveSurveyId(surveyId)

  ServiceFactory.surveyService.fetchById(surveyId).then((json) => {
    const browserLangCode = BrowserUtils.determineBrowserLanguageCode()
    const language = Arrays.contains(json.languages, browserLangCode) ? browserLangCode : json.languages[0]
    dispatch(receiveFullActiveSurvey(json, language))
  })
}

const invalidateActiveSurvey = (activeSurvey) => ({
  type: INVALIDATE_ACTIVE_SURVEY,
  activeSurvey,
})

export const selectActiveSurveyLanguage = (language) => ({
  type: SELECT_ACTIVE_SURVEY_LANGUAGE,
  language,
})
