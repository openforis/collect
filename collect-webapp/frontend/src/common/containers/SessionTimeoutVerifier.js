import React, { Component } from 'react';
import { connect } from 'react-redux'

import Dialog from '@material-ui/core/Dialog';
import DialogTitle from '@material-ui/core/DialogTitle';
import DialogContent from '@material-ui/core/DialogContent';
import DialogContentText from '@material-ui/core/DialogContentText';
import DialogActions from '@material-ui/core/DialogActions';
import LinearProgress from '@material-ui/core/LinearProgress';
import Button from '@material-ui/core/Button';

import {sessionExpired} from 'actions/session'
import Preloader from 'common/components/Preloader'
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

    componentWillUnmount() {
        clearTimeout(this.timer)
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
        const {sessionExpired} = this.props

        const newOutOfServiceTime = this.state.outOfServiceTime + this.intervalPeriod
        if (newOutOfServiceTime > this.sessionTimeout) {
            clearTimeout(this.timer)
            this.setState({initializing: false, active: false, sessionExpired: true})
            sessionExpired()
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
                            <Button color="primary" variant="contained"
                                onClick={this.handleRefreshButtonClick}>{L.l('global.refresh')}</Button>
                        }
                    </DialogActions>
                </Dialog>
            </div>
        )
    }
}

export default connect(null, {sessionExpired})(SessionTimeoutVerifier)