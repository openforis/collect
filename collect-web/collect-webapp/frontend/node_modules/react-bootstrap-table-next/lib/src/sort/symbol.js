"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _react = require("react");

var _react2 = _interopRequireDefault(_react);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

var SortSymbol = function SortSymbol() {
  return _react2.default.createElement(
    "span",
    { className: "order" },
    _react2.default.createElement(
      "span",
      { className: "dropdown" },
      _react2.default.createElement("span", { className: "caret" })
    ),
    _react2.default.createElement(
      "span",
      { className: "dropup" },
      _react2.default.createElement("span", { className: "caret" })
    )
  );
};

exports.default = SortSymbol;