import { useSelector } from 'react-redux'

export const SurveySelectors = {
  useSurvey: () => useSelector((state) => state?.activeSurvey?.survey),
  useSurveyId: () => useSelector((state) => state?.activeSurvey?.survey?.id),
}
