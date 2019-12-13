'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.default = toPath;

var _lodash = require('lodash.topath');

var _lodash2 = _interopRequireDefault(_lodash);

var _endsWith = require('./ends-with');

var _endsWith2 = _interopRequireDefault(_endsWith);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function toPath(value) {
  var path = value;

  if ((0, _endsWith2.default)(path, '.')) {
    path = path.slice(0, -1);
  } else if ((0, _endsWith2.default)(path, '[]')) {
    path = path.slice(0, -2);
  }

  return (0, _lodash2.default)(path);
}