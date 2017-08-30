import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { bindActionCreators } from 'redux'
import { connect } from 'react-redux'
import * as Actions from './actions';

class Startup extends Component {
    static propTypes = {
        session: PropTypes.object
    }
    componentDidMount() {
        this.props.actions.fetchCurrentUser();
        this.props.actions.fetchUsers();
        this.props.actions.fetchUserGroups();
    }
    render() {
        return this.props.session
            ? this.props.children
            : (<p>Loading...</p>);
    }
}

function mapStateToProps(state) {
    return {
        session: state.session
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