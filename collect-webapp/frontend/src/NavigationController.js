import { useEffect } from 'react'
import { useHistory } from 'react-router-dom'
import { connect } from 'react-redux'

import ServiceFactory from 'services/ServiceFactory'
import RouterUtils from 'utils/RouterUtils'

const checkShouldNavigateToRecordEditPage = async ({ user, history }) => {
  const surveySummaries = await ServiceFactory.surveyService.fetchAllSummaries()
  const publishedSurveys = surveySummaries.filter((surveySummary) => surveySummary.published)
  if (publishedSurveys.length === 1) {
    const surveyId = publishedSurveys[0].id
    const { id: userId } = user
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

const NavigationController = (props) => {
  const { user } = props

  const history = useHistory()

  useEffect(() => {
    checkShouldNavigateToRecordEditPage({ user, history })
  }, [])

  return null
}

const mapStateToProps = (state) => {
  const user = state.session.loggedUser

  return {
    user,
  }
}

export default connect(mapStateToProps)(NavigationController)
