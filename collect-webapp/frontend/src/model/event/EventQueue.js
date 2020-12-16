import { showSystemError } from 'actions/systemError'
import Queue from 'utils/Queue'

class EventQueueInternal {
  queuesByEventType = {}
  subscribersByEvent = {}
  processing = false
  size = 0
  dispatch = null // to be set on app initialization

  _getQueue(eventType) {
    let queue = this.queuesByEventType[eventType]
    if (!queue) {
      queue = new Queue()
      this.queuesByEventType[eventType] = queue
    }
    return queue
  }

  publish(eventType, data) {
    const queue = this._getQueue(eventType)
    queue.enqueue(data)
    this.size = this.size + 1

    if (!this.processing) {
      this.startProcessing()
    }
  }

  startProcessing() {
    this.processing = true

    const $this = this
    Object.entries(this.queuesByEventType).forEach(([eventType, queue]) => {
      const data = queue.dequeue()
      $this.processItem({ eventType, data })
    })
    this.processing = false

    if (!this.isEmpty()) {
      this.startProcessing()
    }
  }

  processItem({ eventType, data }) {
    try {
      const subscribers = this.subscribersByEvent[eventType]
      if (subscribers) {
        subscribers.forEach((s) => s(data))
      }
      this.size -= 1
    } catch (error) {
      const { message, stack: stackTrace } = error
      this.dispatch(showSystemError({ message, stackTrace }))
    }
  }

  isEmpty() {
    return this.size === 0
  }

  subscribe(event, callback) {
    let subscribers = this.subscribersByEvent[event]
    if (!subscribers) {
      subscribers = []
      this.subscribersByEvent[event] = subscribers
    }
    subscribers.push(callback)
  }

  unsubscribe(event, callback) {
    const subscribers = this.subscribersByEvent[event]
    const subscriberIdx = subscribers.indexOf(callback)
    delete subscribers[subscriberIdx]
  }
}

export default class EventQueue {
  static internalQueue = new EventQueueInternal()

  static publish(eventType, data) {
    EventQueue.internalQueue.publish(eventType, data)
  }

  static subscribe(eventType, callback) {
    EventQueue.internalQueue.subscribe(eventType, callback)
  }

  static unsubscribe(eventType, callback) {
    EventQueue.internalQueue.unsubscribe(eventType, callback)
  }

  static set dispatch(dispatch) {
    EventQueue.internalQueue.dispatch = dispatch
  }
}
