'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.default = containsEvent;
function containsEvent(events, event) {
  if (typeof events === 'string') {
    return events === event;
  }

  return !!~events.indexOf(event);
}