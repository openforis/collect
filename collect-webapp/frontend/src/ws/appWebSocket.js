import React from 'react'
import { useDispatch } from 'react-redux'
import SockJsClient from 'react-stomp'

import Constants from '../Constants'
import { fetchSurveySummaries } from 'actions/surveys'
import { recordLocked, recordUnlocked } from '../datamanagement/actions'
import EventQueue from 'model/event/EventQueue'
import { RecordEvent, RecordEventWrapper } from 'model/event/RecordEvent'
import { showSystemError } from 'actions/systemError'
import ServiceFactory from 'services/ServiceFactory'

const eventsDestination = '/events'

const messageTypes = {
  surveyUpdated: 'SURVEY_UPDATED',
  surveyDeleted: 'SURVEY_DELETED',
  surveysUpdated: 'SURVEYS_UPDATED',
  recordLocked: 'RECORD_LOCKED',
  recordUnlocked: 'RECORD_UNLOCKED',
  recordUpdated: 'RECORD_UPDATED',
  recordUpdateError: 'RECORD_UPDATE_ERROR',
}

const handlersByType = {
  // TODO
  [messageTypes.surveyUpdated]: (message) => () => {
    const { surveyId } = message
    ServiceFactory.codeListService.invalidateCache({ surveyId })
  },
  // TODO
  [messageTypes.surveyDeleted]: (message) => () => {
    const { surveyId } = message
    ServiceFactory.codeListService.invalidateCache({ surveyId })
  },
  [messageTypes.surveysUpdated]: fetchSurveySummaries,
  [messageTypes.recordLocked]: (message) => recordLocked(message.recordId, message.lockedBy),
  [messageTypes.recordUnlocked]: (message) => recordUnlocked(message.recordId),
  [messageTypes.recordUpdated]: (message) => () => {
    const eventWrapper = new RecordEventWrapper(message.event)
    EventQueue.publish(RecordEvent.TYPE, eventWrapper.event)
  },
  [messageTypes.recordUpdateError]: (content) => (dispatch) => {
    dispatch(showSystemError(content))
  },
}

const AppWebSocket = () => {
  const dispatch = useDispatch()

  const onMessage = (message) => {
    const handler = handlersByType[message.type]
    dispatch(handler(message))
  }

  return <SockJsClient url={`${Constants.BASE_URL}ws`} topics={[eventsDestination]} onMessage={onMessage} />
}

export default AppWebSocket
