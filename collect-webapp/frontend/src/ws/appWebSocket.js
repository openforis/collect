import React from 'react'
import { useDispatch, useSelector } from 'react-redux'
import SockJsClient from 'react-stomp'

import Constants from '../Constants'
import EventQueue from 'model/event/EventQueue'
import { RecordEvent, RecordEventWrapper } from 'model/event/RecordEvent'

import ServiceFactory from 'services/ServiceFactory'

import { showSystemError } from 'actions/systemError'
import { clearActiveSurvey } from 'actions/activeSurvey'
import { fetchSurveySummaries } from 'actions/surveys'
import { recordLocked, recordUnlocked } from '../datamanagement/actions'

const eventsDestination = '/events'

const MESSAGE_TYPES = {
  //TODO surveyUpdated: 'SURVEY_UPDATED',
  surveyDeleted: 'SURVEY_DELETED',
  surveyPublished: 'SURVEY_PUBLISHED',
  surveyUnpublished: 'SURVEY_UNPUBLISHED',
  surveysUpdated: 'SURVEYS_UPDATED',
  recordLocked: 'RECORD_LOCKED',
  recordUnlocked: 'RECORD_UNLOCKED',
  recordUpdated: 'RECORD_UPDATED',
  recordUpdateError: 'RECORD_UPDATE_ERROR',
}

const AppWebSocket = () => {
  const dispatch = useDispatch()

  const { survey: activeSurvey } = useSelector((state) => state.activeSurvey)

  const invalidateSurveyCache = ({ surveyId }) => {
    ServiceFactory.codeListService.invalidateCache({ surveyId })
    if (activeSurvey?.id === surveyId) {
      dispatch(clearActiveSurvey())
    }
  }

  const messageHandlersByType = {
    // TODO
    // [messageTypes.surveyUpdated]: (message) => () => {
    //   const { surveyId } = message
    //   ServiceFactory.codeListService.invalidateCache({ surveyId })
    // },
    [MESSAGE_TYPES.surveyPublished]: (message) => () => {
      const { surveyId } = message
      invalidateSurveyCache({ surveyId })
    },
    [MESSAGE_TYPES.surveyUnpublished]: (message) => () => {
      const { surveyId } = message
      invalidateSurveyCache({ surveyId })
    },
    [MESSAGE_TYPES.surveyDeleted]: (message) => () => {
      const { surveyId } = message
      invalidateSurveyCache({ surveyId })
    },
    [MESSAGE_TYPES.surveysUpdated]: fetchSurveySummaries,
    [MESSAGE_TYPES.recordLocked]: (message) => recordLocked(message.recordId, message.lockedBy),
    [MESSAGE_TYPES.recordUnlocked]: (message) => recordUnlocked(message.recordId),
    [MESSAGE_TYPES.recordUpdated]: (message) => () => {
      const eventWrapper = new RecordEventWrapper(message.event)
      EventQueue.publish(RecordEvent.TYPE, eventWrapper.event)
    },
    [MESSAGE_TYPES.recordUpdateError]: (content) => (dispatch) => {
      dispatch(showSystemError(content))
    },
  }

  const onMessage = (message) => {
    const handler = messageHandlersByType[message.type]
    dispatch(handler(message))
  }

  return <SockJsClient url={`${Constants.BASE_URL}ws`} topics={[eventsDestination]} onMessage={onMessage} />
}

export default AppWebSocket
