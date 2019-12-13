'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.default = shallowCompareWithoutChildren;

var _react = require('react');

var _shallowEqual = require('./shallow-equal');

var _shallowEqual2 = _interopRequireDefault(_shallowEqual);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function shallowCompareWithoutChildren(instance, nextProps) {
  return !(0, _shallowEqual2.default)(instance.props, nextProps, { omitKeys: ['children'] }) || _react.Children.count(instance.props.children) !== _react.Children.count(nextProps.children);
}