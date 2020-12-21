import React, { useEffect } from 'react'
import { useDispatch, useSelector } from 'react-redux'

import * as Actions from 'actions'
import * as SurveysActions from 'actions/surveys'
import * as SessionActions from 'actions/session'
import * as UserActions from 'actions/users'
import * as UserGroupActions from 'actions/usergroups'

import Preloader from 'common/components/Preloader'
import { ActiveSurveyLocalStorage } from 'localStorage'
import { selectActiveSurvey } from 'actions/activeSurvey'

const Startup = (props) => {
  const dispatch = useDispatch()

  useEffect(() => {
    dispatch(Actions.fetchApplicationInfo())
    dispatch(SessionActions.fetchCurrentUser())
    dispatch(UserActions.fetchUsers())
    dispatch(UserGroupActions.fetchUserGroups())
    dispatch(SurveysActions.fetchSurveySummaries())
  }, [])

  const { isFetching: isFetchingLoggedUser, initialized: isLoggedUserReady } = useSelector(
    (state) => state.session
  ) || {
    loggedUser: null,
    isLoggedUserReady: false,
    isFetchingLoggedUser: true,
    sessionExpired: false,
    loggingOut: false,
    loggedOut: false,
  }

  const { initialized: surveySummariesReady, items: surveySummaries } = useSelector(
    (state) => state.surveyDesigner.surveysList
  )

  const { isFetching: isFetchingUsers, initialized: isUsersReady } = useSelector((state) => state.users) || {
    isUsersReady: false,
    isFetchingUsers: true,
  }

  const { isFetching: isFetchingUserGroups, initialized: isUserGroupsReady } = useSelector(
    (state) => state.userGroups
  ) || {
    isUserGroupsReady: true,
    isFetchingUserGroups: true,
  }

  // get active survey from local storage and set it in UI
  useEffect(() => {
    if (surveySummariesReady) {
      const activeSurveyId = ActiveSurveyLocalStorage.getActiveSurveyId()
      if (activeSurveyId && surveySummaries.some((surveySummary) => surveySummary.id === activeSurveyId)) {
        dispatch(selectActiveSurvey(activeSurveyId))
      }
    }
  }, [surveySummariesReady])

  const loading =
    !isLoggedUserReady ||
    isFetchingLoggedUser ||
    !isUsersReady ||
    isFetchingUsers ||
    !isUserGroupsReady ||
    isFetchingUserGroups

  const { children } = props

  return <Preloader loading={loading}>{children}</Preloader>
}

export default Startup
