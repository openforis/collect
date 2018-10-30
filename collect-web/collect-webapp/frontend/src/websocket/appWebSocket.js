import React from 'react'
import { connect } from 'react-redux'
import SockJsClient from 'react-stomp'

import Constants from '../Constants'
import {surveyCreated, surveyUpdated, surveyDeleted} from 'actions/surveys'
import handleSurveyMessage from './surveyMessageHandler'

const eventsDestination = '/events'

const handleMessage = (props, message) => {
    handleSurveyMessage(props, message)
}

const AppWebSocket = props => {
    return <SockJsClient url={`${Constants.BASE_URL}/ws`}
                         topics={[eventsDestination]}
                         onMessage={message => handleMessage(props, message)} />
}

function mapStateToProps(state) {
    return state
}

export default connect(
    mapStateToProps, 
    {
        surveyCreated, 
        surveyUpdated, 
        surveyDeleted
    }
)(AppWebSocket)