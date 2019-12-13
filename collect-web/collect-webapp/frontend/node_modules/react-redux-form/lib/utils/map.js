"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.default = map;
function map(values, iteratee) {
  if (Array.isArray(values)) {
    return values.map(iteratee);
  }

  var result = Object.keys(values).map(function (key) {
    return iteratee(values[key], key, values);
  });

  return result;
}