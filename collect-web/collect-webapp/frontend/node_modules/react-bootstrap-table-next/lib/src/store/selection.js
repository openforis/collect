'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.getSelectedRows = exports.unSelectableKeys = exports.selectableKeys = exports.isAnySelectedRow = exports.isSelectedAll = undefined;

var _utils = require('../utils');

var _utils2 = _interopRequireDefault(_utils);

var _rows = require('./rows');

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

var isSelectedAll = exports.isSelectedAll = function isSelectedAll(_ref) {
  var data = _ref.data,
      selected = _ref.selected;
  return data.length === selected.length;
};

var isAnySelectedRow = exports.isAnySelectedRow = function isAnySelectedRow(_ref2) {
  var selected = _ref2.selected;
  return function () {
    var skips = arguments.length > 0 && arguments[0] !== undefined ? arguments[0] : [];

    if (skips.length === 0) {
      return selected.length > 0;
    }
    return selected.filter(function (x) {
      return !skips.includes(x);
    }).length;
  };
};

var selectableKeys = exports.selectableKeys = function selectableKeys(_ref3) {
  var data = _ref3.data,
      keyField = _ref3.keyField;
  return function () {
    var skips = arguments.length > 0 && arguments[0] !== undefined ? arguments[0] : [];

    if (skips.length === 0) {
      return data.map(function (row) {
        return _utils2.default.get(row, keyField);
      });
    }
    return data.filter(function (row) {
      return !skips.includes(_utils2.default.get(row, keyField));
    }).map(function (row) {
      return _utils2.default.get(row, keyField);
    });
  };
};

var unSelectableKeys = exports.unSelectableKeys = function unSelectableKeys(_ref4) {
  var selected = _ref4.selected;
  return function () {
    var skips = arguments.length > 0 && arguments[0] !== undefined ? arguments[0] : [];

    if (skips.length === 0) {
      return [];
    }
    return selected.filter(function (x) {
      return skips.includes(x);
    });
  };
};

var getSelectedRows = exports.getSelectedRows = function getSelectedRows(store) {
  var getRow = (0, _rows.getRowByRowId)(store);
  return store.selected.map(function (k) {
    return getRow(k);
  });
};