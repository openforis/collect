"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.default = findKey;
function findKey(object, predicate) {
  var resultKey = void 0;

  Object.keys(object).some(function (key) {
    var isKey = predicate(object[key], key, object);

    if (isKey) {
      resultKey = key;
      return true;
    }

    return false;
  });

  return resultKey;
}