'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.default = invertValidity;

var _isPlainObject = require('./is-plain-object');

var _isPlainObject2 = _interopRequireDefault(_isPlainObject);

var _mapValues = require('./map-values');

var _mapValues2 = _interopRequireDefault(_mapValues);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function invertValidity(validity) {
  if ((0, _isPlainObject2.default)(validity)) {
    return (0, _mapValues2.default)(validity, invertValidity);
  }

  return !validity;
}