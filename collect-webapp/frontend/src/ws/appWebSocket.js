import React from 'react'
import { connect } from 'react-redux'
import SockJsClient from 'react-stomp'

import Constants from '../Constants'
import { fetchSurveySummaries } from 'actions/surveys'

const eventsDestination = '/events'

const messageTypes = {
    surveysUpdated: 'SURVEYS_UPDATED',
}

const handleMessage = (props, message) => {
    switch (message.type) {
        case messageTypes.surveysUpdated:
            props.fetchSurveySummaries()
            break
        default:
    }
}

const AppWebSocket = props => {
    return <SockJsClient url={`${Constants.BASE_URL}ws`}
        topics={[eventsDestination]}
        onMessage={message => handleMessage(props, message)} />
}

function mapStateToProps(state) {
    return state
}

export default connect(
    mapStateToProps,
    {
        fetchSurveySummaries,
    }
)(AppWebSocket)