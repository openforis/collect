'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.dispatchBatchIfNeeded = undefined;

var _slicedToArray = function () { function sliceIterator(arr, i) { var _arr = []; var _n = true; var _d = false; var _e = undefined; try { for (var _i = arr[Symbol.iterator](), _s; !(_n = (_s = _i.next()).done); _n = true) { _arr.push(_s.value); if (i && _arr.length === i) break; } } catch (err) { _d = true; _e = err; } finally { try { if (!_n && _i["return"]) _i["return"](); } finally { if (_d) throw _e; } } return _arr; } return function (arr, i) { if (Array.isArray(arr)) { return arr; } else if (Symbol.iterator in Object(arr)) { return sliceIterator(arr, i); } else { throw new TypeError("Invalid attempt to destructure non-iterable instance"); } }; }();

var _actionTypes = require('../action-types');

var _actionTypes2 = _interopRequireDefault(_actionTypes);

var _partition3 = require('../utils/partition');

var _partition4 = _interopRequireDefault(_partition3);

var _isPlainObject = require('../utils/is-plain-object');

var _isPlainObject2 = _interopRequireDefault(_isPlainObject);

var _track = require('../utils/track');

var _nullAction = require('../constants/null-action');

var _nullAction2 = _interopRequireDefault(_nullAction);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

var batch = (0, _track.trackable)(function (model, actions) {
  var dispatchableActions = actions.filter(function (action) {
    return !!action;
  });

  if (!dispatchableActions.length) return _nullAction2.default;

  if (dispatchableActions.length && dispatchableActions.every(_isPlainObject2.default)) {
    if (dispatchableActions.length === 1) {
      return dispatchableActions[0];
    }

    return {
      type: _actionTypes2.default.BATCH,
      model: model,
      actions: dispatchableActions
    };
  }

  var _partition = (0, _partition4.default)(dispatchableActions, function (action) {
    return typeof action !== 'function';
  }),
      _partition2 = _slicedToArray(_partition, 2),
      plainActions = _partition2[0],
      actionThunks = _partition2[1];

  if (!actionThunks.length) {
    if (plainActions.length > 1) {
      return {
        type: _actionTypes2.default.BATCH,
        model: model,
        actions: plainActions
      };
    } else if (plainActions.length === 1) {
      return plainActions[0];
    }
  }

  return function (dispatch) {
    if (plainActions.length > 1) {
      dispatch({
        type: _actionTypes2.default.BATCH,
        model: model,
        actions: plainActions
      });
    } else if (plainActions.length === 1) {
      dispatch(plainActions[0]);
    }
    actionThunks.forEach(dispatch);
  };
});

function dispatchBatchIfNeeded(model, actions, dispatch) {
  if (!actions.length) return void 0;

  var dispatchableActions = actions.filter(function (action) {
    return !!action;
  });

  if (!dispatchableActions.length) return void 0;

  return dispatch(batch(model, dispatchableActions));
}

exports.default = batch;
exports.dispatchBatchIfNeeded = dispatchBatchIfNeeded;