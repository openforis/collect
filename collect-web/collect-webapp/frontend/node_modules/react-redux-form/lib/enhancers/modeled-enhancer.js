'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.createModelReducerEnhancer = undefined;

var _modelReducer2 = require('../reducers/model-reducer');

var _modelReducer3 = _interopRequireDefault(_modelReducer2);

var _nullAction = require('../constants/null-action');

var _nullAction2 = _interopRequireDefault(_nullAction);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function createModelReducerEnhancer() {
  var modelReducerCreator = arguments.length > 0 && arguments[0] !== undefined ? arguments[0] : _modelReducer3.default;
  var options = arguments.length > 1 && arguments[1] !== undefined ? arguments[1] : {};

  return function modelReducerEnhancer(reducer, model) {
    var initialState = void 0;
    try {
      initialState = reducer(undefined, _nullAction2.default);
    } catch (error) {
      initialState = null;
    }

    var _modelReducer = modelReducerCreator(model, initialState, options);

    return function () {
      var state = arguments.length > 0 && arguments[0] !== undefined ? arguments[0] : initialState;
      var action = arguments[1];

      var updatedState = _modelReducer(state, action);

      return reducer(updatedState, action);
    };
  };
}

var modelReducerEnhancer = createModelReducerEnhancer(_modelReducer3.default);

exports.createModelReducerEnhancer = createModelReducerEnhancer;
exports.default = modelReducerEnhancer;