import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { bindActionCreators } from 'redux'
import { connect } from 'react-redux'
import * as Actions from './actions';

class Startup extends Component {
    
    static propTypes = {
        loggedUser: PropTypes.object
    }

    componentDidMount() {
        this.props.actions.fetchCurrentUser();
        this.props.actions.fetchUsers();
        this.props.actions.fetchUserGroups();
    }

    render() {
        const p = this.props
        if (p.isFetchingLoggedUser || p.isFetchingUsers|| p.isFetchingUserGroups ) {
            return <p>Loading...</p>
        } else {
            return this.props.children
        }
    }
}

function mapStateToProps(state) {
    const {
        loggedUser,
        isFetching: isFetchingLoggedUser
    } = state.session || {
        isFetchingLoggedUser: true
    }

    const {
        users,
        isFetching: isFetchingUsers
    } = state.users || {
        isFetchingUsers: true
    }

    const {
        userGroups,
        isFetching: isFetchingUserGroups
    } = state.userGroups || {
        isFetchingUserGroups: true
    }

    return {
        isFetchingLoggedUser,
        loggedUser,
        isFetchingUsers,
        users,
        isFetchingUserGroups,
        userGroups
    };
}

function mapDispatchToProps(dispatch) {
    return {
        actions: bindActionCreators(Actions, dispatch)
    };
}

export default connect(
    mapStateToProps,
    mapDispatchToProps
)(Startup);