'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.default = getFormValue;

var _mapValues = require('./map-values');

var _mapValues2 = _interopRequireDefault(_mapValues);

var _toArray = require('./to-array');

var _toArray2 = _interopRequireDefault(_toArray);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function getFormValue(form) {
  if (form && !form.$form) {
    return typeof form.loadedValue !== 'undefined' ? form.loadedValue : form.initialValue;
  }

  var result = (0, _mapValues2.default)(form, function (field, key) {
    if (key === '$form') return undefined;

    return getFormValue(field);
  });

  delete result.$form;

  var isArray = form && form.$form && Array.isArray(form.$form.value);

  return isArray ? (0, _toArray2.default)(result) : result;
}