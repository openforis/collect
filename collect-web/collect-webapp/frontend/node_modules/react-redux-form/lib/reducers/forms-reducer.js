'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.createForms = exports.createFormCombiner = undefined;

var _extends = Object.assign || function (target) { for (var i = 1; i < arguments.length; i++) { var source = arguments[i]; for (var key in source) { if (Object.prototype.hasOwnProperty.call(source, key)) { target[key] = source[key]; } } } return target; };

var _modeledEnhancer = require('../enhancers/modeled-enhancer');

var _modeledEnhancer2 = _interopRequireDefault(_modeledEnhancer);

var _modelReducer = require('./model-reducer');

var _modelReducer2 = _interopRequireDefault(_modelReducer);

var _formReducer = require('./form-reducer');

var _formReducer2 = _interopRequireDefault(_formReducer);

var _redux = require('redux');

var _identity = require('../utils/identity');

var _identity2 = _interopRequireDefault(_identity);

var _nullAction = require('../constants/null-action');

var _nullAction2 = _interopRequireDefault(_nullAction);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _defineProperty(obj, key, value) { if (key in obj) { Object.defineProperty(obj, key, { value: value, enumerable: true, configurable: true, writable: true }); } else { obj[key] = value; } return obj; }

function _objectWithoutProperties(obj, keys) { var target = {}; for (var i in obj) { if (keys.indexOf(i) >= 0) continue; if (!Object.prototype.hasOwnProperty.call(obj, i)) continue; target[i] = obj[i]; } return target; }

var defaults = {
  key: 'forms',
  plugins: []
};

function getSubModelString(model, subModel) {
  if (!model) return subModel;

  return model + '.' + subModel;
}

var defaultStrategy = {
  modelReducer: _modelReducer2.default,
  formReducer: _formReducer2.default,
  modeled: _modeledEnhancer2.default,
  toJS: _identity2.default
};

function createFormCombiner() {
  var strategy = arguments.length > 0 && arguments[0] !== undefined ? arguments[0] : defaultStrategy;

  function createForms(forms) {
    var model = arguments.length > 1 && arguments[1] !== undefined ? arguments[1] : '';
    var options = arguments.length > 2 && arguments[2] !== undefined ? arguments[2] : {};

    var formKeys = Object.keys(forms);
    var modelReducers = {};
    var initialFormState = {};
    var optionsWithDefaults = _extends({}, defaults, options);

    var key = optionsWithDefaults.key,
        plugins = optionsWithDefaults.plugins,
        formOptions = _objectWithoutProperties(optionsWithDefaults, ['key', 'plugins']);

    formKeys.forEach(function (formKey) {
      var formValue = forms[formKey];
      var subModel = getSubModelString(model, formKey);

      if (typeof formValue === 'function') {
        var initialState = void 0;
        try {
          initialState = formValue(undefined, _nullAction2.default);
        } catch (error) {
          initialState = null;
        }

        modelReducers[formKey] = strategy.modeled(formValue, subModel);
        initialFormState[formKey] = initialState;
      } else {
        modelReducers[formKey] = strategy.modelReducer(subModel, formValue);
        initialFormState[formKey] = strategy.toJS(formValue);
      }
    });

    return _extends({}, modelReducers, _defineProperty({}, key, function (state, action) {
      return strategy.formReducer(model, initialFormState, _extends({
        plugins: plugins
      }, formOptions))(state, action);
    }));
  }

  function combineForms(forms) {
    var model = arguments.length > 1 && arguments[1] !== undefined ? arguments[1] : '';
    var options = arguments.length > 2 && arguments[2] !== undefined ? arguments[2] : {};

    var mappedReducers = createForms(forms, model, options);

    return (0, _redux.combineReducers)(mappedReducers);
  }

  return {
    createForms: createForms,
    combineForms: combineForms
  };
}

var _createFormCombiner = createFormCombiner(),
    defaultCombineForms = _createFormCombiner.combineForms,
    defaultCreateForms = _createFormCombiner.createForms;

exports.default = defaultCombineForms;
exports.createFormCombiner = createFormCombiner;
exports.createForms = defaultCreateForms;