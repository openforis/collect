'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _actionTypes = require('../action-types');

var _actionTypes2 = _interopRequireDefault(_actionTypes);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function createBatchReducer(reducer, initialState) {
  var options = arguments.length > 2 && arguments[2] !== undefined ? arguments[2] : {};
  var transformAction = options.transformAction;


  return function () {
    var state = arguments.length > 0 && arguments[0] !== undefined ? arguments[0] : initialState;
    var action = arguments[1];

    var transformedAction = transformAction ? transformAction(action) : action;

    if (transformedAction.type === _actionTypes2.default.BATCH) {
      return transformedAction.actions.reduce(reducer, state);
    }

    return reducer(state, transformedAction);
  };
}

exports.default = createBatchReducer;