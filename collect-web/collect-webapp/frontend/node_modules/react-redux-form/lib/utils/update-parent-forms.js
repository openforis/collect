'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.default = updateParentForms;

var _icepick = require('icepick');

var _icepick2 = _interopRequireDefault(_icepick);

var _get = require('./get');

var _get2 = _interopRequireDefault(_get);

var _assocIn = require('./assoc-in');

var _assocIn2 = _interopRequireDefault(_assocIn);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _toConsumableArray(arr) { if (Array.isArray(arr)) { for (var i = 0, arr2 = Array(arr.length); i < arr.length; i++) { arr2[i] = arr[i]; } return arr2; } else { return Array.from(arr); } }

// import { updateFieldState } from './create-field';

function updateParentForms(state, localPath, updater) {
  var parentLocalPath = localPath.slice(0, -1);

  var value = parentLocalPath.length ? (0, _get2.default)(state, parentLocalPath) : state;

  if (!value) return state;

  var form = value.$form;

  var updatedValue = typeof updater === 'function' ? updater(value) : updater;

  // const updatedForm = updateFieldState(form, updatedValue);

  var newState = (0, _assocIn2.default)(state, [].concat(_toConsumableArray(parentLocalPath), ['$form']), _icepick2.default.merge(form, updatedValue));

  if (!parentLocalPath.length) return newState;

  return updateParentForms(newState, parentLocalPath, updater);
}