"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.default = arraysEqual;
function arraysEqual(firstArray, secondArray) {
  return firstArray && secondArray && firstArray.length === secondArray.length && firstArray.every(function (item, index) {
    return item === secondArray[index];
  });
}