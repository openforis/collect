"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.default = pick;
function pick(object, props) {
  var result = {};

  for (var i = 0; i < props.length; i++) {
    var prop = props[i];
    result[prop] = object[prop];
  }

  return result;
}