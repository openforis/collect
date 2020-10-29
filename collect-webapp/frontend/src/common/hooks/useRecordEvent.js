import { useEffect } from 'react'

import EventQueue from 'model/event/EventQueue'
import { RecordEvent } from 'model/event/RecordEvent'

export const useRecordEvent = ({ parentEntity, onEvent }) => {
  const handleRecordEventReceived = (event) => {
    if (!parentEntity) {
      return
    }
    onEvent(event)
  }

  useEffect(() => {
    EventQueue.subscribe(RecordEvent.TYPE, handleRecordEventReceived)
    return () => EventQueue.unsubscribe(RecordEvent.TYPE, handleRecordEventReceived)
  }, [parentEntity])
}
