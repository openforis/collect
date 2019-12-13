'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _bootstrapTable = require('./src/bootstrap-table');

var _bootstrapTable2 = _interopRequireDefault(_bootstrapTable);

var _container = require('./src/container');

var _container2 = _interopRequireDefault(_container);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

exports.default = (0, _container2.default)(_bootstrapTable2.default);