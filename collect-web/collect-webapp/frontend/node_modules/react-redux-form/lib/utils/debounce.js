"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.default = debounce;
function debounce(func, delay) {
  var timeout = void 0;
  var laterFunc = void 0;

  var createLaterFunc = function createLaterFunc(args) {
    return function () {
      timeout = null;
      func.apply(null, args);

      // Only run the deferred function once
      laterFunc = undefined;
    };
  };

  var debouncedFunc = function debouncedFunc() {
    for (var _len = arguments.length, args = Array(_len), _key = 0; _key < _len; _key++) {
      args[_key] = arguments[_key];
    }

    clearTimeout(timeout);
    laterFunc = createLaterFunc(args);
    timeout = setTimeout(laterFunc, delay);
  };

  debouncedFunc.flush = function () {
    clearTimeout(timeout);
    if (laterFunc) laterFunc();
  };

  debouncedFunc.cancel = function () {
    clearTimeout(timeout);
    laterFunc = undefined;
  };

  return debouncedFunc;
}