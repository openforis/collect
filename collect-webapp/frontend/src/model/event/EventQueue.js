import Queue from 'utils/Queue'

class EventQueueInternal {
  queuesByEvent = {}
  subscribersByEvent = {}
  processing = false
  size = 0

  _getQueue(event) {
    let queue = this.queuesByEvent[event]
    if (!queue) {
      queue = new Queue()
      this.queuesByEvent[event] = queue
    }
    return queue
  }

  publish(event, data) {
    const queue = this._getQueue(event)
    queue.enqueue(data)
    this.size = this.size + 1

    if (!this.processing) {
      this.startProcessing()
    }
  }

  startProcessing() {
    const $this = this
    Object.entries(this.queuesByEvent).forEach(([event, queue]) => {
      const data = queue.dequeue()
      const subscribers = $this.subscribersByEvent[event]
      if (subscribers) {
        subscribers.forEach((s) => s(data))
      }
      $this.size = $this.size - 1
    })
    this.processing = false

    if (!this.isEmpty()) {
      this.startProcessing()
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

  static publish(event, data) {
    EventQueue.internalQueue.publish(event, data)
  }

  static subscribe(event, callback) {
    EventQueue.internalQueue.subscribe(event, callback)
  }

  static unsubscribe(event, callback) {
    EventQueue.internalQueue.unsubscribe(event, callback)
  }
}
