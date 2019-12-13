'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _typeof = typeof Symbol === "function" && typeof Symbol.iterator === "symbol" ? function (obj) { return typeof obj; } : function (obj) { return obj && typeof Symbol === "function" && obj.constructor === Symbol && obj !== Symbol.prototype ? "symbol" : typeof obj; };

exports.default = immutableGetFromState;

var _toPath = require('./to-path');

var _toPath2 = _interopRequireDefault(_toPath);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function immutableGetFromState(state, modelString) {
  var path = (0, _toPath2.default)(modelString);

  return path.reduce(function (subState, subPath) {
    if (!subState || typeof subState === 'string') return subState;

    // Current subState is immutable
    if ((typeof subState === 'undefined' ? 'undefined' : _typeof(subState)) === 'object' && 'get' in subState) {
      return subState.get(subPath);
    }

    // Current subState is a plain object/array
    return subState[subPath];
  }, state);
}