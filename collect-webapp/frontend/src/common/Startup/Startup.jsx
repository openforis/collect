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
  const { children } = props

  const dispatch = useDispatch()

  const { isFetching: fetchingLoggedUser, initialized: loggedUserReady } = useSelector((state) => state.session) || {
    loggedUserReady: false,
    fetchingLoggedUser: true,
  }

  const { isFetching: fetchingUsers, initialized: usersReady } = useSelector((state) => state.users) || {
    usersReady: false,
    fetchingUsers: true,
  }

  const { isFetching: fetchingUserGroups, initialized: userGroupsReady } = useSelector((state) => state.userGroups) || {
    userGroupsReady: false,
    fetchingUserGroups: true,
  }

  const loading =
    !loggedUserReady || fetchingLoggedUser || !usersReady || fetchingUsers || !userGroupsReady || fetchingUserGroups

  useEffect(() => {
    dispatch(Actions.fetchApplicationInfo())
    dispatch(SessionActions.fetchCurrentUser())
    dispatch(UserActions.fetchUsers())
    dispatch(UserGroupActions.fetchUserGroups())
    dispatch(SurveysActions.fetchSurveySummaries())
  }, [])

  return (
    <Preloader loading={loading}>
      <AppWebSocket />
      <StartupActiveSurvey />
      {children}
    </Preloader>
  )
}

export default Startup
