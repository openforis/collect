import React, { Component } from 'react'
import { connect } from 'react-redux'
import * as Actions from 'actions'
import * as SurveysActions from 'actions/surveys'
import * as SessionActions from 'actions/session'
import * as UserActions from 'actions/users'
import * as UserGroupActions from 'actions/usergroups'
import Preloader from 'components/Preloader'

class Startup extends Component {
    
    static SURVEY_RELOAD_TIMEOUT = 5000

    surveyReloadTimer

    constructor(props) {
        super(props)

        this.handleSurveysReloadTimeout = this.handleSurveysReloadTimeout.bind(this)
    }

    componentDidMount() {
        this.props.dispatch(Actions.fetchApplicationInfo())
        this.props.dispatch(SessionActions.fetchCurrentUser())
        this.props.dispatch(UserActions.fetchUsers())
        this.props.dispatch(UserGroupActions.fetchUserGroups())
        this.props.dispatch(SurveysActions.fetchSurveySummaries())
        
        //this.startReloadinInfoTimer()
    }

    componentWillReceiveProps(nextProps) {
        const { sessionExpired, loggingOut, loggedOut } = nextProps
        if (sessionExpired || loggingOut || loggedOut) {
            this.stopReloadingInfoTimer()
        }
    }

    startReloadinInfoTimer() {
        this.surveyReloadTimer = setTimeout(this.handleSurveysReloadTimeout, Startup.SURVEY_RELOAD_TIMEOUT)        
    }
    
    stopReloadingInfoTimer() {
        if (this.surveyReloadTimer) {
            clearTimeout(this.surveyReloadTimer)
        }
    }

    handleSurveysReloadTimeout() {
        this.props.dispatch(SurveysActions.fetchSurveySummaries())
        this.startReloadinInfoTimer()
    }

    render() {
        const p = this.props
        const loading = !p.isLoggedUserReady || p.isFetchingLoggedUser 
            || !p.isUsersReady || p.isFetchingUsers
            || !p.isUserGroupsReady || p.isFetchingUserGroups
        return (
            <Preloader loading={loading}>
                {this.props.children}
            </Preloader>
        )
    }
}

function mapStateToProps(state) {
    const {
        loggedUser,
        isFetching: isFetchingLoggedUser,
        initialized: isLoggedUserReady,
        sessionExpired,
        loggingOut,
        loggedOut
    } = state.session || {
        loggedUser: null,
        isLoggedUserReady: false,
        isFetchingLoggedUser: true,
        sessionExpired: false,
        loggingOut: false,
        loggedOut: false
    }

    const {
        users,
        isFetching: isFetchingUsers,
        initialized: isUsersReady
    } = state.users || {
        isUsersReady: false,
        isFetchingUsers: true
    }

    const {
        userGroups,
        isFetching: isFetchingUserGroups,
        initialized: isUserGroupsReady
    } = state.userGroups || {
        isUserGroupsReady: true,
        isFetchingUserGroups: true
    }

    return {
        isLoggedUserReady,
        isFetchingLoggedUser,
        loggedUser,
        loggingOut,
        sessionExpired,
        loggedOut,
        isUsersReady,
        isFetchingUsers,
        users,
        isUserGroupsReady,
        isFetchingUserGroups,
        userGroups
    }
}

export default connect(
    mapStateToProps
)(Startup)