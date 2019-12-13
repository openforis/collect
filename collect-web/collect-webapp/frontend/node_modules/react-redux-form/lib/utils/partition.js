"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.default = partition;
function partition(collection, predicate) {
  var result = [[], []];

  collection.forEach(function (item, index) {
    if (predicate(item, index, collection)) {
      result[0].push(item);
    } else {
      result[1].push(item);
    }
  });

  return result;
}