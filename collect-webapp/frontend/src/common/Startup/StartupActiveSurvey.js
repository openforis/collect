import { useEffect } from 'react'
import { useDispatch, useSelector } from 'react-redux'

import { ActiveSurveyLocalStorage } from 'localStorage'
import { clearActiveSurvey, selectActiveSurvey } from 'actions/activeSurvey'

const StartupActiveSurvey = () => {
  const dispatch = useDispatch()

  const { initialized: surveySummariesReady, items: surveySummaries } = useSelector(
    (state) => state.surveyDesigner.surveysList
  )

  // get active survey from local storage and set it in UI
  useEffect(() => {
    if (surveySummariesReady) {
      const activeSurveyId = ActiveSurveyLocalStorage.getActiveSurveyId()
      if (activeSurveyId) {
        // active survey exists an it has not been deleted
        if (surveySummaries.some((surveySummary) => surveySummary.id === activeSurveyId)) {
          dispatch(selectActiveSurvey(activeSurveyId))
        } else {
          // active survey has been deleted: remove it from local storage
          dispatch(clearActiveSurvey())
        }
      }
    }
  }, [surveySummariesReady])

  return null
}

export default StartupActiveSurvey
