'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _extends = Object.assign || function (target) { for (var i = 1; i < arguments.length; i++) { var source = arguments[i]; for (var key in source) { if (Object.prototype.hasOwnProperty.call(source, key)) { target[key] = source[key]; } } } return target; };

var _fieldActions = require('./field-actions');

var _fieldActions2 = _interopRequireDefault(_fieldActions);

var _modelActions = require('./model-actions');

var _modelActions2 = _interopRequireDefault(_modelActions);

var _batchActions = require('./batch-actions');

var _batchActions2 = _interopRequireDefault(_batchActions);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

var actions = _extends({}, _fieldActions2.default, _modelActions2.default, {
  batch: _batchActions2.default
});

exports.default = actions;