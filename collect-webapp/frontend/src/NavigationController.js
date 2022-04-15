import { useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { connect } from 'react-redux'

import User from 'model/User'

import ServiceFactory from 'services/ServiceFactory'
import RouterUtils from 'utils/RouterUtils'

const checkShouldNavigateToRecordEditPage = async ({ user, navigate }) => {
  if (user.role !== User.ROLE.ENTRY_LIMITED) return false

  const surveySummaries = await ServiceFactory.surveyService.fetchAllSummaries()
  const publishedSurveys = surveySummaries.filter((surveySummary) => surveySummary.published)
  if (publishedSurveys.length !== 1) return false

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
  if (count !== 1) return false

  const recordId = records[0].id
  RouterUtils.navigateToRecordEditPage(navigate, recordId)
  return true
}

const NavigationController = (props) => {
  const { user } = props

  const navigate = useNavigate()

  useEffect(() => {
    checkShouldNavigateToRecordEditPage({ user, navigate })
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
