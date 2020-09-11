import React from 'react'
import { useDispatch } from 'react-redux'
import SockJsClient from 'react-stomp'

import Constants from '../Constants'
import { fetchSurveySummaries } from 'actions/surveys'
import { recordLocked, recordUnlocked } from '../datamanagement/actions'
import EventQueue from '../model/event/EventQueue'
import { RecordEventWrapper } from '../model/event/RecordEvent'

const eventsDestination = '/events'

const messageTypes = {
  surveysUpdated: 'SURVEYS_UPDATED',
  recordLocked: 'RECORD_LOCKED',
  recordUnlocked: 'RECORD_UNLOCKED',
  recordUpdated: 'RECORD_UPDATED',
}

const handlersByType = {
  [messageTypes.surveysUpdated]: () => fetchSurveySummaries,
  [messageTypes.recordLocked]: (message) => recordLocked(message.recordId, message.lockedBy),
  [messageTypes.recordUnlocked]: (message) => recordUnlocked(message.recordId),
  [messageTypes.recordUpdated]: (message) => () => {
    const eventWrapper = new RecordEventWrapper(message.event)
    EventQueue.publish('recordEvent', eventWrapper.event)
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
