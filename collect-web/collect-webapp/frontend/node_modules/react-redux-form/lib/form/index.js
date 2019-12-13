'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.isRetouched = exports.isTouched = exports.isPending = exports.isValid = undefined;

var _extends = Object.assign || function (target) { for (var i = 1; i < arguments.length; i++) { var source = arguments[i]; for (var key in source) { if (Object.prototype.hasOwnProperty.call(source, key)) { target[key] = source[key]; } } } return target; };

exports.default = formSelector;

var _isValid = require('./is-valid');

var _isValid2 = _interopRequireDefault(_isValid);

var _isPending = require('./is-pending');

var _isPending2 = _interopRequireDefault(_isPending);

var _isTouched = require('./is-touched');

var _isTouched2 = _interopRequireDefault(_isTouched);

var _isRetouched = require('./is-retouched');

var _isRetouched2 = _interopRequireDefault(_isRetouched);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function formSelector(formState) {
  return _extends({}, formState, {
    get valid() {
      return (0, _isValid2.default)(formState);
    },
    get pending() {
      return (0, _isPending2.default)(formState);
    },
    get touched() {
      return (0, _isTouched2.default)(formState);
    },
    get retouched() {
      return (0, _isRetouched2.default)(formState);
    }
  });
}

exports.isValid = _isValid2.default;
exports.isPending = _isPending2.default;
exports.isTouched = _isTouched2.default;
exports.isRetouched = _isRetouched2.default;