'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.default = updateSubFields;

var _get = require('./get');

var _get2 = _interopRequireDefault(_get);

var _assocIn = require('./assoc-in');

var _assocIn2 = _interopRequireDefault(_assocIn);

var _createField = require('./create-field');

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function updateSubField(subField, newSubState) {
  // Form
  if (subField && subField.$form) {
    // intermediate value - not mutated outside function
    var result = {};

    Object.keys(subField).forEach(function (key) {
      if (key === '$form') {
        result.$form = (0, _createField.updateFieldState)(subField.$form, newSubState);
      } else {
        result[key] = updateSubField(subField[key], newSubState);
      }
    });

    return result;
  }

  // Field
  return (0, _createField.updateFieldState)(subField, newSubState);
}

function updateSubFields(state, localPath, newState) {
  var field = (0, _get2.default)(state, localPath);

  // only forms can have fields -
  // skip if field is not a form
  if (!field || !field.$form) return state;

  // intermediate value - not mutated outside function
  var updatedField = {};

  Object.keys(field).forEach(function (key) {
    if (key === '$form') {
      updatedField.$form = field.$form;
    } else {
      updatedField[key] = updateSubField(field[key], newState);
    }
  });

  if (!localPath.length) return updatedField;

  return (0, _assocIn2.default)(state, localPath, updatedField);
}