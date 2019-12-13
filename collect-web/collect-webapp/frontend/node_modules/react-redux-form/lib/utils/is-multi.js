'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.default = isMulti;

var _endsWith = require('./ends-with');

var _endsWith2 = _interopRequireDefault(_endsWith);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function isMulti(model) {
  return (0, _endsWith2.default)(model, '[]');
}