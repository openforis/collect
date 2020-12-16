import React, { Component } from 'react'
import { connect } from 'react-redux'

import { sessionExpired } from 'actions/session'
import Preloader from 'common/components/Preloader'
import ServiceFactory from 'services/ServiceFactory'
import RouterUtils from 'utils/RouterUtils'
import L from 'utils/Labels'
import Objects from 'utils/Objects'
import SystemErrorDialog from './SystemErrorDialog'

const SESSION_TIMEOUT = 60000
const PING_PERIOD = 20000

class SessionTimeoutVerifier extends Component {
  timer

  constructor(props) {
    super(props)

    this.state = {
      initializing: true,
      active: true,
      sessionExpired: false,
      outOfServiceTime: 0,
    }

    this.ping = this.ping.bind(this)
    this.startTimer = this.startTimer.bind(this)
    this.handleTimeout = this.handleTimeout.bind(this)
    this.handlePingResponse = this.handlePingResponse.bind(this)
    this.handlePingError = this.handlePingError.bind(this)
  }

  componentDidMount() {
    this.ping()
  }

  componentWillUnmount() {
    clearTimeout(this.timer)
  }

  startTimer() {
    this.timer = setTimeout(this.handleTimeout, PING_PERIOD)
  }

  handleTimeout() {
    this.ping()
  }

  ping() {
    ServiceFactory.sessionService.fetchCurrentUser().then(this.handlePingResponse, this.handlePingError)
  }

  handlePingResponse(user) {
    const { initializing, outOfServiceTime } = this.state

    if (user == null) {
      this.setState({ initializing: false, active: false, sessionExpired: true })
    } else {
      let newState = {}
      if (initializing) {
        newState = { ...newState, initializing: false }
      }
      this.startTimer()
      if (outOfServiceTime > 0) {
        newState = { ...newState, outOfServiceTime: 0 }
      }
      if (Objects.isNotEmpty(newState)) {
        this.setState(newState)
      }
    }
  }

  handlePingError() {
    const { sessionExpired } = this.props
    const { outOfServiceTime } = this.state

    const newOutOfServiceTime = outOfServiceTime + PING_PERIOD
    const newState = { initializing: false, active: false }
    if (newOutOfServiceTime > SESSION_TIMEOUT) {
      clearTimeout(this.timer)
      this.setState({ ...newState, sessionExpired: true })
      sessionExpired()
    } else {
      this.startTimer()
      this.setState({ ...newState, outOfServiceTime: newOutOfServiceTime })
    }
  }

  render() {
    const { active, initializing, sessionExpired } = this.state

    if (initializing) {
      return <Preloader />
    }
    const errorMessage = sessionExpired
      ? L.l('connection.sessionExpired')
      : L.l('connection.serverConnectionError.message')

    return (
      <>
        {this.props.children}
        {!active && (
          <SystemErrorDialog
            title={'connection.serverConnectionError.title'}
            message={errorMessage}
            showProgressBar={!sessionExpired}
          />
        )}
      </>
    )
  }
}

export default connect(null, { sessionExpired })(SessionTimeoutVerifier)
