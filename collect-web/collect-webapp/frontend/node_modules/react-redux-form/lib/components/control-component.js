'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.createControlClass = undefined;

var _actions = require('../actions');

var _actions2 = _interopRequireDefault(_actions);

var _get2 = require('../utils/get');

var _get3 = _interopRequireDefault(_get2);

var _getFieldFromState = require('../utils/get-field-from-state');

var _getFieldFromState2 = _interopRequireDefault(_getFieldFromState);

var _controlComponentFactory = require('./control-component-factory');

var _controlComponentFactory2 = _interopRequireDefault(_controlComponentFactory);

var _reactDom = require('react-dom');

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

var defaultStrategy = {
  get: _get3.default,
  getFieldFromState: _getFieldFromState2.default,
  actions: _actions2.default,
  findDOMNode: _reactDom.findDOMNode
};

exports.createControlClass = _controlComponentFactory2.default;
exports.default = (0, _controlComponentFactory2.default)(defaultStrategy);