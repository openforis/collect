'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.default = get;

var _lodash = require('lodash.get');

var _lodash2 = _interopRequireDefault(_lodash);

var _endsWith = require('./ends-with');

var _endsWith2 = _interopRequireDefault(_endsWith);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function get(object, path, defaultValue) {
  var modelString = path;

  if (typeof path === 'number') {
    var result = object[path];

    return result === undefined ? defaultValue : result;
  }

  if (!path.length) return object;

  if ((0, _endsWith2.default)(modelString, '.')) {
    modelString = modelString.slice(0, -1);
  } else if ((0, _endsWith2.default)(modelString, '[]')) {
    modelString = modelString.slice(0, -2);
  }

  return (0, _lodash2.default)(object, modelString, defaultValue);
}