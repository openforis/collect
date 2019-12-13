"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.default = toArray;
function toArray(object) {
  var result = [];
  Object.keys(object).forEach(function (key) {
    if (object.hasOwnProperty(key)) {
      result.push(object[key]);
    }
  });
  return result;
}