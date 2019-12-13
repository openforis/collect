'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _extends = Object.assign || function (target) { for (var i = 1; i < arguments.length; i++) { var source = arguments[i]; for (var key in source) { if (Object.prototype.hasOwnProperty.call(source, key)) { target[key] = source[key]; } } } return target; };

exports.default = combineForms;

var _modeledEnhancer = require('./enhancers/modeled-enhancer');

var _modeledEnhancer2 = _interopRequireDefault(_modeledEnhancer);

var _modelReducer = require('./reducers/model-reducer');

var _modelReducer2 = _interopRequireDefault(_modelReducer);

var _formReducer = require('./reducers/form-reducer');

var _formReducer2 = _interopRequireDefault(_formReducer);

var _redux = require('redux');

var _redux2 = _interopRequireDefault(_redux);

var _nullAction = require('./constants/null-action');

var _nullAction2 = _interopRequireDefault(_nullAction);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _defineProperty(obj, key, value) { if (key in obj) { Object.defineProperty(obj, key, { value: value, enumerable: true, configurable: true, writable: true }); } else { obj[key] = value; } return obj; }

function combineForms(forms) {
  var formReducerKey = arguments.length > 1 && arguments[1] !== undefined ? arguments[1] : 'forms';

  var formKeys = Object.keys(forms);
  var modelReducers = {};
  var initialFormState = {};

  formKeys.forEach(function (formKey) {
    var formValue = forms[formKey];

    if (typeof formValue === 'function') {
      var initialState = void 0;
      try {
        initialState = formValue(undefined, _nullAction2.default);
      } catch (error) {
        initialState = null;
      }

      modelReducers[formKey] = (0, _modeledEnhancer2.default)(formValue, formKey);
      initialFormState[formKey] = initialState;
    } else {
      modelReducers[formKey] = (0, _modelReducer2.default)(formKey, formValue);
      initialFormState[formKey] = formValue;
    }
  });

  return (0, _redux2.default)(_extends({}, modelReducers, _defineProperty({}, formReducerKey, (0, _formReducer2.default)('', initialFormState))));
}