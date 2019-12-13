'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.createModeler = undefined;

var _get2 = require('../utils/get');

var _get3 = _interopRequireDefault(_get2);

var _icepick = require('icepick');

var _icepick2 = _interopRequireDefault(_icepick);

var _arraysEqual = require('../utils/arrays-equal');

var _arraysEqual2 = _interopRequireDefault(_arraysEqual);

var _toPath = require('../utils/to-path');

var _toPath2 = _interopRequireDefault(_toPath);

var _actionTypes = require('../action-types');

var _actionTypes2 = _interopRequireDefault(_actionTypes);

var _batchedEnhancer = require('../enhancers/batched-enhancer');

var _batchedEnhancer2 = _interopRequireDefault(_batchedEnhancer);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function icepickSet(state, path, value) {
  return _icepick2.default.setIn(state, path, value);
}

var defaultStrategy = {
  get: _get3.default,
  set: icepickSet,
  object: {}
};

function createModeler() {
  var strategy = arguments.length > 0 && arguments[0] !== undefined ? arguments[0] : defaultStrategy;
  var getter = strategy.get,
      setter = strategy.set,
      object = strategy.object;


  return function _createModelReducer(model) {
    var initialState = arguments.length > 1 && arguments[1] !== undefined ? arguments[1] : object;
    var options = arguments.length > 2 && arguments[2] !== undefined ? arguments[2] : {};

    var modelPath = (0, _toPath2.default)(model);

    var modelReducer = function modelReducer() {
      var state = arguments.length > 0 && arguments[0] !== undefined ? arguments[0] : initialState;
      var action = arguments[1];

      if (!action.model) {
        return state;
      }

      var path = (0, _toPath2.default)(action.model);

      if (!(0, _arraysEqual2.default)(path.slice(0, modelPath.length), modelPath)) {
        return state;
      }

      var localPath = path.slice(modelPath.length);

      switch (action.type) {
        case _actionTypes2.default.CHANGE:
          if (!localPath.length) {
            return action.value;
          }

          if (getter(state, localPath) === action.value) {
            return state;
          }

          return setter(state, localPath, action.value);

        case _actionTypes2.default.RESET:
          if (!localPath.length) {
            return initialState;
          }

          if (getter(state, localPath) === getter(initialState, localPath)) {
            return state;
          }

          return setter(state, localPath, getter(initialState, localPath));

        default:
          return state;
      }
    };

    return (0, _batchedEnhancer2.default)(modelReducer, initialState, options);
  };
}

var modelReducer = createModeler();

exports.createModeler = createModeler;
exports.default = modelReducer;