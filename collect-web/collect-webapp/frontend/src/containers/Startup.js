import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { connect } from 'react-redux'
import * as Actions from 'actions';
import * as UserActions from 'actions/users';
import * as SurveysActions from 'actions/surveys';
import Labels from 'utils/Labels';

class Startup extends Component {
    
    static SURVEY_RELOAD_TIMEOUT = 5000

    constructor(props) {
        super(props)

        this.handleSurveysReloadTimeout = this.handleSurveysReloadTimeout.bind(this)
    }

    static propTypes = {
        loggedUser: PropTypes.object
    }

    componentDidMount() {
        Labels.initialize(() => {
            this.props.dispatch(Actions.fetchApplicationInfo())
            this.props.dispatch(Actions.fetchCurrentUser())
            this.props.dispatch(UserActions.fetchUsers())
            this.props.dispatch(Actions.fetchUserGroups())
            this.props.dispatch(SurveysActions.fetchSurveySummaries());
            
            setInterval(this.handleSurveysReloadTimeout, Startup.SURVEY_RELOAD_TIMEOUT);
        })
    }

    handleSurveysReloadTimeout() {
        this.props.dispatch(SurveysActions.fetchSurveySummaries());
    }

    render() {
        const p = this.props
        if (!p.isLoggedUserReady || p.isFetchingLoggedUser 
            || !p.isUsersReady || p.isFetchingUsers
            || !p.isUserGroupsReady || p.isFetchingUserGroups ) {
            return <p>Loading...</p>
        } else {
            return this.props.children
        }
    }
}

function mapStateToProps(state) {
    const {
        loggedUser,
        isFetching: isFetchingLoggedUser,
        initialized: isLoggedUserReady
    } = state.session || {
        isLoggedUserReady: false,
        isFetchingLoggedUser: true
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
        isUsersReady,
        isFetchingUsers,
        users,
        isUserGroupsReady,
        isFetchingUserGroups,
        userGroups
    };
}

export default connect(
    mapStateToProps
)(Startup);