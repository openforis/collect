import React from 'react'
import { connect } from 'react-redux'
import SockJsClient from 'react-stomp'

import Constants from '../Constants'
import {surveyCreated, surveyUpdated, surveyDeleted} from 'actions/surveys'

const surveysUpdateDestination = '/surveys/update'
const surveyUpdateTypes = {
    created: 'CREATED',
    updated: 'UPDATED',
    published: 'PUBLISHED',
    unpublished: 'UNPUBLISHED',
    deleted: 'DELETED'
}
const handleSurveysUpdatedMessage = (props, message) => {
    const {surveyCreated, surveyUpdated, surveyDeleted} = props
    const {updateType, survey} = message

    switch(updateType) {
        case surveyUpdateTypes.created:
            surveyCreated(survey)
            break
        case surveyUpdateTypes.updated:
        case surveyUpdateTypes.published:
        case surveyUpdateTypes.unpublished:
            surveyUpdated(survey)
            break
        case surveyUpdateTypes.deleted:
            surveyDeleted(survey)
            break
    }
}

const AppWebSocket = props => {
    return <SockJsClient url={`${Constants.BASE_URL}/ws`}
                         topics={[surveysUpdateDestination]}
                         onMessage={message => handleSurveysUpdatedMessage(props, message)} />
}

function mapStateToProps(state) {
    return state
}

export default connect(mapStateToProps, {surveyCreated, surveyUpdated, surveyDeleted})(AppWebSocket)