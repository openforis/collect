import React, { Component } from 'react';
import { Modal, ModalHeader, ModalBody, ModalFooter, Button, Progress } from 'reactstrap';
import { connect } from 'react-redux';

import * as SessionActions from 'actions/session'
import ServiceFactory from 'services/ServiceFactory'
import RouterUtils from 'utils/RouterUtils'

class SessionTimeoutVerifier extends Component {
    
    timer
    intervalPeriod = 20000
    sessionTimeout = 60000

    constructor(props) {
        super(props)

        this.state = {
            initializing: true,
            active: false,
            sessionExpired: false,
            outOfServiceTime: 0
        }

        this.ping = this.ping.bind(this)
        this.startTimer = this.startTimer.bind(this)
        this.handleTimeout = this.handleTimeout.bind(this)
        this.handlePingResponse = this.handlePingResponse.bind(this)
        this.handlePingError = this.handlePingError.bind(this)
        this.handleRefreshButtonClick = this.handleRefreshButtonClick.bind(this)
    }

    componentDidMount() {
        this.ping()
    }

    startTimer() {
        this.timer = setTimeout(this.handleTimeout, this.intervalPeriod)
    }

    handleTimeout() {
        this.ping()
    }

    ping() {
        ServiceFactory.sessionService.fetchCurrentUser().then(this.handlePingResponse, this.handlePingError)
    }

    handlePingResponse(user) {
        if (user == null) {
            this.setState({initializing: false, active: false, sessionExpired: true})
        } else {
            this.startTimer()
            this.setState({initializing: false, active: true, sessionExpired: false, outOfServiceTime: 0})
        }
    }

    handlePingError(error) {
        const newOutOfServiceTime = this.state.outOfServiceTime + this.intervalPeriod
        if (newOutOfServiceTime > this.sessionTimeout) {
            clearTimeout(this.timer)
            this.setState({initializing: false, active: false, sessionExpired: true})
            this.props.dispatch(SessionActions.sessionExpired())
        } else {
            this.startTimer()
            this.setState({initializing: false, active: false, outOfServiceTime: newOutOfServiceTime})
        }
    }

    handleRefreshButtonClick() {
        RouterUtils.navigateToHomePage()
    }

    render() {
        if (this.state.initializing) {
            return <div>Loading...</div>
        }
        const errorMessage = this.state.sessionExpired ? 'Session expired. Refresh the web browser page.': 
            'Error connecting to the server; trying to establish the connection again...'
        return (
            <div>
                {this.props.children}
                <Modal isOpen={! this.state.active} backdrop="static">
                    <ModalHeader>Server connection error</ModalHeader>
                    <ModalBody>{errorMessage}
                        {! this.state.sessionExpired ? 
                            <Progress animated value="100" />
                        : ''}
                    </ModalBody>
                    <ModalFooter>
                        {this.state.sessionExpired ? 
                            <Button color="primary"
                                onClick={this.handleRefreshButtonClick}>Refresh</Button>
                        : ''}
                    </ModalFooter>
                </Modal>
            </div>
        )
    }
}

function mapStateToProps(state) {
    return {}
}    

export default connect(
    mapStateToProps
)(SessionTimeoutVerifier);