import React, { Component } from 'react';
import Dialog, {
    DialogActions,
    DialogContent,
    DialogContentText,
    DialogTitle,
  } from 'material-ui/Dialog';
import { LinearProgress } from 'material-ui/Progress';
import Button from 'material-ui/Button';
import { connect } from 'react-redux';

import * as SessionActions from 'actions/session'
import Preloader from 'components/Preloader'
import ServiceFactory from 'services/ServiceFactory'
import RouterUtils from 'utils/RouterUtils'
import L from 'utils/Labels';

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
            return <Preloader />
        }
        const errorMessage = this.state.sessionExpired ? L.l('connection.sessionExpired'): L.l('connection.serverConnectionError.message')
            
        return (
            <div>
                {this.props.children}
                <Dialog open={! this.state.active} 
                    ignoreBackdropClick
                    ignoreEscapeKeyUp>
                    <DialogTitle>{L.l('connection.serverConnectionError.title')}</DialogTitle>
                    <DialogContent style={{width: '400px'}}>
                        <DialogContentText>{errorMessage}</DialogContentText>
                        {! this.state.sessionExpired && 
                            <LinearProgress />
                        }
                    </DialogContent>
                    <DialogActions>
                        {this.state.sessionExpired && 
                            <Button color="primary" raised
                                onClick={this.handleRefreshButtonClick}>{L.l('global.refresh')}</Button>
                        }
                    </DialogActions>
                </Dialog>
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