"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.default = find;
function find() {
  var array = arguments.length > 0 && arguments[0] !== undefined ? arguments[0] : [];
  var predicate = arguments[1];

  if (array.prototype.find) return array.find(predicate);

  var length = array.length >>> 0;

  for (var i = 0; i < length; i++) {
    var value = array[i];

    if (predicate(value, i, array)) return value;
  }

  return undefined;
}