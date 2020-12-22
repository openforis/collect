import React, { useEffect } from 'react'
import { useDispatch, useSelector } from 'react-redux'

import * as Actions from 'actions'
import * as SurveysActions from 'actions/surveys'
import * as SessionActions from 'actions/session'
import * as UserActions from 'actions/users'
import * as UserGroupActions from 'actions/usergroups'

import AppWebSocket from 'ws/appWebSocket'
import Preloader from 'common/components/Preloader'
import StartupActiveSurvey from './StartupActiveSurvey'

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

  const loading =
    !isLoggedUserReady ||
    isFetchingLoggedUser ||
    !isUsersReady ||
    isFetchingUsers ||
    !isUserGroupsReady ||
    isFetchingUserGroups

  const { children } = props

  return (
    <Preloader loading={loading}>
      <AppWebSocket />
      <StartupActiveSurvey />
      {children}
    </Preloader>
  )
}

export default Startup
