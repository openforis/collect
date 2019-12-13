'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.nextOrder = exports.sort = undefined;

var _utils = require('../utils');

var _utils2 = _interopRequireDefault(_utils);

var _const = require('../const');

var _const2 = _interopRequireDefault(_const);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _toConsumableArray(arr) { if (Array.isArray(arr)) { for (var i = 0, arr2 = Array(arr.length); i < arr.length; i++) { arr2[i] = arr[i]; } return arr2; } else { return Array.from(arr); } } /* eslint no-nested-ternary: 0 */
/* eslint no-lonely-if: 0 */
/* eslint no-underscore-dangle: 0 */


function comparator(a, b) {
  var result = void 0;
  if (typeof b === 'string') {
    result = b.localeCompare(a);
  } else {
    result = a > b ? -1 : a < b ? 1 : 0;
  }
  return result;
}

var sort = exports.sort = function sort(_ref) {
  var data = _ref.data,
      sortOrder = _ref.sortOrder,
      sortField = _ref.sortField;
  return function (sortFunc) {
    var _data = [].concat(_toConsumableArray(data));
    _data.sort(function (a, b) {
      var result = void 0;
      var valueA = _utils2.default.get(a, sortField);
      var valueB = _utils2.default.get(b, sortField);
      valueA = _utils2.default.isDefined(valueA) ? valueA : '';
      valueB = _utils2.default.isDefined(valueB) ? valueB : '';

      if (sortFunc) {
        result = sortFunc(valueA, valueB, sortOrder, sortField);
      } else {
        if (sortOrder === _const2.default.SORT_DESC) {
          result = comparator(valueA, valueB);
        } else {
          result = comparator(valueB, valueA);
        }
      }
      return result;
    });
    return _data;
  };
};

var nextOrder = exports.nextOrder = function nextOrder(store) {
  return function (field, order) {
    var defaultOrder = arguments.length > 2 && arguments[2] !== undefined ? arguments[2] : _const2.default.SORT_DESC;

    if (order) return order;

    if (field !== store.sortField) {
      return defaultOrder;
    }
    return store.sortOrder === _const2.default.SORT_DESC ? _const2.default.SORT_ASC : _const2.default.SORT_DESC;
  };
};