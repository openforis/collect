import { useEffect } from 'react'
import { useSelector } from 'react-redux'
import { useHistory } from 'react-router'

import ServiceFactory from 'services/ServiceFactory'
import RouterUtils from 'utils/RouterUtils'

const checkShouldNavigateToRecordEditPage = async ({ userId, history }) => {
  const surveySummaries = await ServiceFactory.surveyService.fetchAllSummaries()
  const publishedSurveys = surveySummaries.filter((surveySummary) => surveySummary.published)
  if (publishedSurveys.length === 1) {
    const surveyId = publishedSurveys[0].id

    const recordsRes = await ServiceFactory.recordService.fetchRecordSummaries({
      surveyId,
      rootEntityName: null,
      userId,
      filterOptions: {
        recordsPerPage: 10,
        page: 1,
        ownerIds: [userId],
        keyValues: [],
        summaryValues: [],
      },
    })
    const { count, records } = recordsRes
    if (count === 1) {
      const recordId = records[0].id
      RouterUtils.navigateToRecordEditPage(history, recordId)
    }
  }
}

export const useCheckShouldNavigateToRecordEditPage = () => {
  const history = useHistory()

  const { loggedUser } = useSelector((state) => state.session)

  useEffect(() => {
    if (loggedUser.canEditOnlyOwnedRecords()) {
      checkShouldNavigateToRecordEditPage({ userId: loggedUser.id, history })
    }
  }, [])
}
