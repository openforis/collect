export default class EventQueue {

  static subscribersByEvent = {};

  static publish(event, data) {
    var subscribers = EventQueue.subscribersByEvent[event];

    if (typeof subscribers === 'undefined') {
      return false;
    }

    subscribers.forEach(s => s(data))

    return true;
  }

  static subscribe(event, callback) {
    var subscribers = EventQueue.subscribersByEvent[event];
    if (typeof subscribers === 'undefined') {
      subscribers = [];
      EventQueue.subscribersByEvent[event] = subscribers
    }
    subscribers.push(callback);
  }

  static unsubscribe(event, callback) {
    var subscribers = EventQueue.subscribersByEvent[event];
    let subscriberIdx = subscribers.indexOf(callback)
    delete subscribers[subscriberIdx]
  }
  
}
  