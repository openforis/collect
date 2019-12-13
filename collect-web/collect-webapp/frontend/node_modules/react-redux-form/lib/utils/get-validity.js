'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.default = getValidity;

var _getValue = require('./get-value');

var _getValue2 = _interopRequireDefault(_getValue);

var _mapValues = require('./map-values');

var _mapValues2 = _interopRequireDefault(_mapValues);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function getValidity(validators, value) {
  var modelValue = (0, _getValue2.default)(value);

  if (typeof validators === 'function') {
    return validators(modelValue);
  }

  return (0, _mapValues2.default)(validators, function (validator) {
    return getValidity(validator, modelValue);
  });
}