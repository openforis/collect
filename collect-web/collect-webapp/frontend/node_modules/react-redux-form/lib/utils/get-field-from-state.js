'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.default = getFieldFromState;

var _get = require('./get');

var _get2 = _interopRequireDefault(_get);

var _toPath = require('./to-path');

var _toPath2 = _interopRequireDefault(_toPath);

var _getForm = require('./get-form');

var _getForm2 = _interopRequireDefault(_getForm);

var _isPlainObject = require('./is-plain-object');

var _isPlainObject2 = _interopRequireDefault(_isPlainObject);

var _invariant = require('invariant');

var _invariant2 = _interopRequireDefault(_invariant);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

var defaultStrategy = {
  getForm: _getForm2.default
};

function getFieldFromState(state, modelString) {
  var s = arguments.length > 2 && arguments[2] !== undefined ? arguments[2] : defaultStrategy;

  var form = state && '$form' in state ? state : s.getForm(state, modelString);

  if (!form || !form.$form) return null;

  if (!modelString.length) return form;

  (0, _invariant2.default)(form, 'Could not find form for "%s" in the store.', modelString);

  var formPath = (0, _toPath2.default)(form.$form.model);
  var fieldPath = (0, _toPath2.default)(modelString).slice(formPath.length);
  var field = (0, _get2.default)(form, fieldPath);

  if (!field) return null;
  if ((0, _isPlainObject2.default)(field) && '$form' in field) return field.$form;

  return field;
}