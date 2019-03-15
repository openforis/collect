import React from 'react'
import { connect } from 'react-redux'
import SockJsClient from 'react-stomp'

import Constants from '../Constants'
import { fetchSurveySummaries } from 'actions/surveys'
import { recordLocked, recordUnlocked } from '../datamanagement/actions'

const eventsDestination = '/events'

const messageTypes = {
    surveysUpdated: 'SURVEYS_UPDATED',
    recordLocked: 'RECORD_LOCKED',
    recordUnlocked: 'RECORD_UNLOCKED',
}

const handleMessage = (props, message) => {
    switch (message.type) {
        case messageTypes.surveysUpdated:
            props.fetchSurveySummaries()
            break
        case messageTypes.recordLocked:
            props.recordLocked(message.recordId, message.lockedBy)
            break
        case messageTypes.recordUnlocked:
            props.recordUnlocked(message.recordId)
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
        recordLocked,
        recordUnlocked
    }
)(AppWebSocket)