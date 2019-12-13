'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.default = getFieldForm;

var _get = require('./get');

var _get2 = _interopRequireDefault(_get);

var _invariant = require('invariant');

var _invariant2 = _interopRequireDefault(_invariant);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function getFieldForm(state, path) {
  var formPath = path.slice(0, -1);

  if (!formPath.length) return state;

  var form = (0, _get2.default)(state, formPath);

  (0, _invariant2.default)(form, 'Could not find form for "%s" in the store.', formPath.join('.'));

  return form;
}