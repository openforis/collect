'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.createInitialState = createInitialState;
exports.default = createFormReducer;

var _get2 = require('../utils/get');

var _get3 = _interopRequireDefault(_get2);

var _arraysEqual = require('../utils/arrays-equal');

var _arraysEqual2 = _interopRequireDefault(_arraysEqual);

var _isPlainObject = require('../utils/is-plain-object');

var _isPlainObject2 = _interopRequireDefault(_isPlainObject);

var _toPath = require('../utils/to-path');

var _toPath2 = _interopRequireDefault(_toPath);

var _composeReducers = require('../utils/compose-reducers');

var _composeReducers2 = _interopRequireDefault(_composeReducers);

var _batchedEnhancer = require('../enhancers/batched-enhancer');

var _batchedEnhancer2 = _interopRequireDefault(_batchedEnhancer);

var _changeActionReducer = require('./form/change-action-reducer');

var _changeActionReducer2 = _interopRequireDefault(_changeActionReducer);

var _formActionsReducer = require('./form-actions-reducer');

var _createField = require('../utils/create-field');

var _createField2 = _interopRequireDefault(_createField);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _toConsumableArray(arr) { if (Array.isArray(arr)) { for (var i = 0, arr2 = Array(arr.length); i < arr.length; i++) { arr2[i] = arr[i]; } return arr2; } else { return Array.from(arr); } }

function createInitialState(model, state) {
  var customInitialFieldState = arguments.length > 2 && arguments[2] !== undefined ? arguments[2] : {};
  var options = arguments.length > 3 && arguments[3] !== undefined ? arguments[3] : {};

  if (Array.isArray(state) || (0, _isPlainObject2.default)(state)) {
    return (0, _createField.createFormState)(model, state, customInitialFieldState, options);
  }

  return (0, _createField2.default)(model, state, customInitialFieldState, options);
}

function wrapFormReducer(plugin, modelPath, initialState) {
  return function () {
    var state = arguments.length > 0 && arguments[0] !== undefined ? arguments[0] : initialState;
    var action = arguments[1];

    if (!action.model) return state;

    var path = (0, _toPath2.default)(action.model);

    if (modelPath.length && !(0, _arraysEqual2.default)(path.slice(0, modelPath.length), modelPath)) {
      return state;
    }

    var localPath = path.slice(modelPath.length);

    return plugin(state, action, localPath);
  };
}

function createFormReducer(model) {
  var initialState = arguments.length > 1 && arguments[1] !== undefined ? arguments[1] : {};
  var options = arguments.length > 2 && arguments[2] !== undefined ? arguments[2] : {};
  var _options$plugins = options.plugins,
      plugins = _options$plugins === undefined ? [] : _options$plugins,
      customInitialFieldState = options.initialFieldState,
      _options$transformAct = options.transformAction,
      transformAction = _options$transformAct === undefined ? null : _options$transformAct;

  var modelPath = (0, _toPath2.default)(model);
  var initialFormState = createInitialState(model, initialState, customInitialFieldState, options);

  var defaultPlugins = [_changeActionReducer2.default, (0, _formActionsReducer.createFormActionsReducer)({ initialFieldState: customInitialFieldState })];

  var wrappedPlugins = plugins.concat(defaultPlugins).map(function (plugin) {
    return wrapFormReducer(plugin, modelPath, initialFormState);
  });

  return (0, _batchedEnhancer2.default)(_composeReducers2.default.apply(undefined, _toConsumableArray(wrappedPlugins)), undefined, {
    transformAction: transformAction
  });
}