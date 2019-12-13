"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.default = compose;
function compose() {
  for (var _len = arguments.length, reducers = Array(_len), _key = 0; _key < _len; _key++) {
    reducers[_key] = arguments[_key];
  }

  return function (state, action) {
    return reducers.reduceRight(function (prevState, reducer) {
      return reducer(prevState, action);
    }, state);
  };
}