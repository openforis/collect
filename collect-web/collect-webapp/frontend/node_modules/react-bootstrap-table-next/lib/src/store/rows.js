"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});
var matchRow = exports.matchRow = function matchRow(keyField, id) {
  return function (row) {
    return row[keyField] === id;
  };
};

var getRowByRowId = exports.getRowByRowId = function getRowByRowId(_ref) {
  var data = _ref.data,
      keyField = _ref.keyField;
  return function (id) {
    return data.find(matchRow(keyField, id));
  };
};