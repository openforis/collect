'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _typeof = typeof Symbol === "function" && typeof Symbol.iterator === "symbol" ? function (obj) { return typeof obj; } : function (obj) { return obj && typeof Symbol === "function" && obj.constructor === Symbol && obj !== Symbol.prototype ? "symbol" : typeof obj; };

exports.createIteratee = createIteratee;
exports.iterateeValue = iterateeValue;

var _identity = require('./identity');

var _identity2 = _interopRequireDefault(_identity);

var _get2 = require('../utils/get');

var _get3 = _interopRequireDefault(_get2);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

var defaultStrategy = {
  get: _get3.default
};

function createIteratee() {
  var s = arguments.length > 0 && arguments[0] !== undefined ? arguments[0] : defaultStrategy;

  function matcher(object) {
    return function (compareObject) {
      if (compareObject === object) return true;

      return Object.keys(object).every(function (key) {
        return s.get(object, key) === s.get(compareObject, key);
      });
    };
  }

  function propChecker(prop) {
    return function (object) {
      return object && !!s.get(object, prop);
    };
  }

  return function (value) {
    if (typeof value === 'function') {
      return value;
    }

    if (value === null) {
      return _identity2.default;
    }

    if ((typeof value === 'undefined' ? 'undefined' : _typeof(value)) === 'object') {
      return matcher(value);
    }

    return propChecker(value);
  };
}

var iteratee = createIteratee();

function iterateeValue(data, value) {
  if (typeof value === 'function') {
    return value(data);
  }

  if (!Array.isArray(value) && (typeof value === 'undefined' ? 'undefined' : _typeof(value)) !== 'object' && typeof value !== 'string') {
    return !!value;
  }

  return iteratee(value)(data);
}

exports.default = iteratee;