import { useEffect } from 'react'
import { useDispatch, useSelector } from 'react-redux'

import { ActiveSurveyLocalStorage } from 'localStorage'
import { clearActiveSurvey, selectActiveSurvey } from 'actions/activeSurvey'

const StartupActiveSurvey = () => {
  const dispatch = useDispatch()

  const { survey: activeSurvey } = useSelector((state) => state.activeSurvey)

  const { initialized: surveySummariesReady, items: surveySummaries } = useSelector(
    (state) => state.surveyDesigner.surveysList
  )

  // get active survey from local storage if not in Redux store and set it in UI
  useEffect(() => {
    if (!activeSurvey && surveySummariesReady) {
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
  }, [activeSurvey, surveySummariesReady])

  return null
}

export default StartupActiveSurvey
