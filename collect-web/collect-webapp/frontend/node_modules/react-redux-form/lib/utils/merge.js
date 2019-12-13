'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.default = mergeDeep;

var _icepick = require('icepick');

var _icepick2 = _interopRequireDefault(_icepick);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function mergeDeep(target, source) {
  return _icepick2.default.merge(target, source);
}