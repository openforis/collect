import { useEffect } from 'react'

import EventQueue from 'model/event/EventQueue'
import { RecordEvent } from 'model/event/RecordEvent'

export const useRecordEvent = ({ parentEntity, onEvent }) => {
  const onRecordEvent = (event) => {
    if (!parentEntity) {
      return
    }
    onEvent(event)
  }

  useEffect(() => {
    EventQueue.subscribe(RecordEvent.TYPE, onRecordEvent)
    return () => EventQueue.unsubscribe(RecordEvent.TYPE, onRecordEvent)
  }, [parentEntity])
}
