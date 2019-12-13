'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.default = isValidityInvalid;

var _isPlainObject = require('./is-plain-object');

var _isPlainObject2 = _interopRequireDefault(_isPlainObject);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function isValidityInvalid(errors) {
  if (Array.isArray(errors)) {
    return errors.some(isValidityInvalid);
  }

  if ((0, _isPlainObject2.default)(errors)) {
    return Object.keys(errors).some(function (key) {
      return isValidityInvalid(errors[key]);
    });
  }

  return !!errors;
}