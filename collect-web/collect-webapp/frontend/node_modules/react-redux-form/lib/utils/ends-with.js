'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.default = endsWith;
function endsWith(string, subString) {
  if (typeof string !== 'string') return false;

  var lastIndex = string.lastIndexOf(subString);

  return lastIndex !== -1 && lastIndex + subString.length === string.length;
}