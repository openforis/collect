'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _extends = Object.assign || function (target) { for (var i = 1; i < arguments.length; i++) { var source = arguments[i]; for (var key in source) { if (Object.prototype.hasOwnProperty.call(source, key)) { target[key] = source[key]; } } } return target; };

exports.fieldOrForm = fieldOrForm;
exports.getMeta = getMeta;
exports.updateFieldState = updateFieldState;
exports.default = createFieldState;
exports.createFormState = createFormState;

var _initialFieldState = require('../constants/initial-field-state');

var _initialFieldState2 = _interopRequireDefault(_initialFieldState);

var _isPlainObject = require('./is-plain-object');

var _isPlainObject2 = _interopRequireDefault(_isPlainObject);

var _mapValues = require('./map-values');

var _mapValues2 = _interopRequireDefault(_mapValues);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

/* eslint-disable no-use-before-define */
function fieldOrForm(model, value, customInitialFieldState) {
  // TODO: create toModel()
  var stringModel = Array.isArray(model) ? model.join('.') : model;

  if (Array.isArray(value) || (0, _isPlainObject2.default)(value)) {
    return createFormState(stringModel, value, customInitialFieldState);
  }

  return createFieldState(stringModel, value, customInitialFieldState);
}
/* eslint-enable no-use-before-define */

function getMeta(fieldLike, prop) {
  if (fieldLike && fieldLike.$form) return fieldLike.$form[prop];

  return fieldLike[prop];
}

function getSubModelString(model, subModel) {
  if (!model) return subModel;

  return model + '.' + subModel;
}

function updateFieldState(existingFieldState, updatedFieldState) {
  var newField = _extends({}, existingFieldState, updatedFieldState);

  return newField;
}

function createFieldState(model, value, customInitialFieldState) {
  return _extends({
    initialValue: value
  }, _initialFieldState2.default, customInitialFieldState, {
    model: model,
    value: value
  });
}

function createFormState(model, values, customInitialFieldState) {
  var options = arguments.length > 3 && arguments[3] !== undefined ? arguments[3] : {};

  return _extends({
    $form: createFieldState(model, values, customInitialFieldState, options)
  }, options.lazy ? undefined : (0, _mapValues2.default)(values, function (value, key) {
    var subModel = getSubModelString(model, key);

    return fieldOrForm(subModel, value, customInitialFieldState);
  }));
}