import React from 'react'
import { connect } from 'react-redux'

import * as Actions from 'actions'
import * as SurveysActions from 'actions/surveys'
import * as SessionActions from 'actions/session'
import * as UserActions from 'actions/users'
import * as UserGroupActions from 'actions/usergroups'

import Preloader from 'components/Preloader'

class Startup extends React.Component {

    componentDidMount() {
        const { dispatch } = this.props

        dispatch(Actions.fetchApplicationInfo())
        dispatch(SessionActions.fetchCurrentUser())
        dispatch(UserActions.fetchUsers())
        dispatch(UserGroupActions.fetchUserGroups())
        dispatch(SurveysActions.fetchSurveySummaries())
    }

    render() {
        const { loading, children } = this.props

        return (
            <Preloader loading={loading}>
                {children}
            </Preloader>
        )
    }
}

function mapStateToProps(state) {
    const {
        isFetching: isFetchingLoggedUser,
        initialized: isLoggedUserReady,
    } = state.session || {
        loggedUser: null,
        isLoggedUserReady: false,
        isFetchingLoggedUser: true,
        sessionExpired: false,
        loggingOut: false,
        loggedOut: false
    }

    const {
        isFetching: isFetchingUsers,
        initialized: isUsersReady
    } = state.users || {
        isUsersReady: false,
        isFetchingUsers: true
    }

    const {
        isFetching: isFetchingUserGroups,
        initialized: isUserGroupsReady
    } = state.userGroups || {
        isUserGroupsReady: true,
        isFetchingUserGroups: true
    }

    const loading = !isLoggedUserReady || isFetchingLoggedUser
        || !isUsersReady || isFetchingUsers
        || !isUserGroupsReady || isFetchingUserGroups

    return {
        loading
    }
}

export default connect(
    mapStateToProps
)(Startup)