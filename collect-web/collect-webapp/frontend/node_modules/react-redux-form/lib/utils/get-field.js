'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.default = getField;

var _isPlainObject = require('./is-plain-object');

var _isPlainObject2 = _interopRequireDefault(_isPlainObject);

var _get = require('./get');

var _get2 = _interopRequireDefault(_get);

var _initialFieldState = require('../constants/initial-field-state');

var _initialFieldState2 = _interopRequireDefault(_initialFieldState);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function getField(state, path) {
  if (process.env.NODE_ENV !== 'production') {
    if (!(0, _isPlainObject2.default)(state)) {
      throw new Error('Could not retrieve field \'' + path + '\' ' + 'from an invalid/empty form state.');
    }
  }

  var result = (0, _get2.default)(state, path, _initialFieldState2.default);

  if ('$form' in result) return result.$form;

  return result;
}