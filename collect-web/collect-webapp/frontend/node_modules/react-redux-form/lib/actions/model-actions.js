'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _extends = Object.assign || function (target) { for (var i = 1; i < arguments.length; i++) { var source = arguments[i]; for (var key in source) { if (Object.prototype.hasOwnProperty.call(source, key)) { target[key] = source[key]; } } } return target; };

exports.createModelActions = createModelActions;

var _get2 = require('../utils/get');

var _get3 = _interopRequireDefault(_get2);

var _identity = require('../utils/identity');

var _identity2 = _interopRequireDefault(_identity);

var _icepick = require('icepick');

var _icepick2 = _interopRequireDefault(_icepick);

var _getValue = require('../utils/get-value');

var _getValue2 = _interopRequireDefault(_getValue);

var _isMulti = require('../utils/is-multi');

var _isMulti2 = _interopRequireDefault(_isMulti);

var _actionTypes = require('../action-types');

var _actionTypes2 = _interopRequireDefault(_actionTypes);

var _mapValues = require('../utils/map-values');

var _mapValues2 = _interopRequireDefault(_mapValues);

var _track = require('../utils/track');

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

var defaultStrategies = {
  get: _get3.default,
  getValue: _getValue2.default,
  splice: _icepick2.default.splice,
  merge: _icepick2.default.merge,
  remove: _icepick2.default.dissoc,
  push: _icepick2.default.push,
  length: function length(value) {
    return value.length;
  },
  object: {},
  array: []
};

function optionsFromArgs(args, index) {
  var options = arguments.length > 2 && arguments[2] !== undefined ? arguments[2] : {};

  if (typeof index === 'undefined') return undefined;

  return _extends({}, options, args[index]);
}

function createModelActions() {
  var s = arguments.length > 0 && arguments[0] !== undefined ? arguments[0] : defaultStrategies;

  var change = function change(model, value) {
    var options = arguments.length > 2 && arguments[2] !== undefined ? arguments[2] : {};

    // option defaults
    var changeOptions = _extends({
      silent: false,
      multi: (0, _isMulti2.default)(model),
      external: true
    }, options);

    if (typeof value === 'function') {
      return function (dispatch, getState) {
        var modelValue = s.get(getState(), model);

        return dispatch(change(model, value(modelValue), options));
      };
    }

    return _extends({
      type: _actionTypes2.default.CHANGE,
      model: model,
      value: s.getValue(value)
    }, changeOptions);
  };

  function createModifierAction(modifier, defaultValue, optionsIndex, getOptions) {
    var actionCreator = function actionCreator(model) {
      for (var _len = arguments.length, args = Array(_len > 1 ? _len - 1 : 0), _key = 1; _key < _len; _key++) {
        args[_key - 1] = arguments[_key];
      }

      return function (dispatch, getState) {
        var modelValue = s.get(getState(), model, defaultValue);
        var value = modifier.apply(undefined, [modelValue].concat(args));

        var options = getOptions ? getOptions.apply(undefined, [value].concat(args)) : undefined;

        dispatch(change(model, value, optionsFromArgs(args, optionsIndex - 1, options)));
      };
    };

    return actionCreator;
  }

  var xor = createModifierAction(function (value, item) {
    var iteratee = arguments.length > 2 && arguments[2] !== undefined ? arguments[2] : function (_item) {
      return _item === item;
    };

    var valueWithoutItem = value.filter(function (_item) {
      return !iteratee(_item);
    });

    return s.length(value) === s.length(valueWithoutItem) ? s.push(value, item) : valueWithoutItem;
  }, s.array, 3);

  var push = createModifierAction(function (value, item) {
    return s.push(value || s.array, item);
  }, s.array, 2);

  var toggle = createModifierAction(function (value) {
    return !value;
  }, undefined, 1);

  var checkWithValue = function checkWithValue(model, value) {
    var options = arguments.length > 2 && arguments[2] !== undefined ? arguments[2] : {};
    var currentValue = options.currentValue;


    if ((0, _isMulti2.default)(model)) {
      var valueWithItem = currentValue || s.array;
      var valueWithoutItem = (valueWithItem || s.array).filter(function (item) {
        return item !== value;
      });
      var multiValue = s.length(valueWithoutItem) === s.length(valueWithItem) ? s.push(valueWithItem, value) : valueWithoutItem;

      return change(model, multiValue);
    }

    if (typeof value === 'undefined') {
      return change(model, !currentValue);
    }
    return change(model, value);
  };

  var check = function check(model, value) {
    return function (dispatch, getState) {
      var modelValue = s.get(getState(), model);

      var action = checkWithValue(model, value, {
        currentValue: modelValue
      });

      dispatch(action);
    };
  };

  var filter = createModifierAction(function (value, iteratee) {
    return value.filter(iteratee);
  }, s.array, 2);

  var reset = function reset(model) {
    return {
      type: _actionTypes2.default.RESET,
      model: model
    };
  };

  var map = createModifierAction(function (value) {
    var iteratee = arguments.length > 1 && arguments[1] !== undefined ? arguments[1] : _identity2.default;
    return value.map(iteratee);
  }, s.array, 2);

  var remove = createModifierAction(function (value, index) {
    return s.splice(value, index, 1);
  }, s.array, 2, function (_, index) {
    return { removeKeys: [index] };
  });

  var move = createModifierAction(function (value, indexFrom, indexTo) {
    if (indexFrom >= s.length(value) || indexTo >= s.length(value)) {
      throw new Error('Error moving array item: invalid bounds ' + indexFrom + ', ' + indexTo);
    }

    var item = s.get(value, indexFrom);
    var removed = s.splice(value, indexFrom, 1);
    var inserted = s.splice(removed, indexTo, 0, item);

    return inserted;
  }, s.array, 3);

  var merge = createModifierAction(s.merge, {}, 3);

  var omit = createModifierAction(function (value) {
    var props = arguments.length > 1 && arguments[1] !== undefined ? arguments[1] : [];

    var propsArray = typeof props === 'string' ? [props] : props;

    var newValue = propsArray.reduce(function (acc, prop) {
      return s.remove(acc, prop);
    }, value);

    return newValue;
  }, {}, 2, function (_, props) {
    return { removeKeys: props };
  });

  var load = function load(model, values, options) {
    return change(model, values, _extends({
      silent: true,
      load: true
    }, options));
  };

  return (0, _mapValues2.default)({
    change: change,
    xor: xor,
    push: push,
    toggle: toggle,
    check: check,
    checkWithValue: checkWithValue,
    filter: filter,
    reset: reset,
    map: map,
    remove: remove,
    move: move,
    merge: merge,
    omit: omit,
    load: load
  }, _track.trackable);
}

exports.default = createModelActions();