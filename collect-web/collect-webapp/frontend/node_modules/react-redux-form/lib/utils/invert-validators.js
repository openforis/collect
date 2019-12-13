'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.default = invertValidators;

var _mapValues = require('./map-values');

var _mapValues2 = _interopRequireDefault(_mapValues);

var _invertValidity = require('./invert-validity');

var _invertValidity2 = _interopRequireDefault(_invertValidity);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function invertValidators(validators) {
  if (typeof validators === 'function') {
    return function (val) {
      return (0, _invertValidity2.default)(validators(val));
    };
  }

  return (0, _mapValues2.default)(validators, invertValidators);
}