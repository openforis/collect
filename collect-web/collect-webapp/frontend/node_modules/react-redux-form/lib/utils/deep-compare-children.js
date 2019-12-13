'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.compareChildren = compareChildren;
exports.default = deepCompareChildren;

var _react = require('react');

var _react2 = _interopRequireDefault(_react);

var _shallowCompare = require('shallow-compare');

var _shallowCompare2 = _interopRequireDefault(_shallowCompare);

var _shallowEqual = require('./shallow-equal');

var _shallowEqual2 = _interopRequireDefault(_shallowEqual);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function compareChildren(props, nextProps) {
  var children = props.children;
  var nextChildren = nextProps.children;

  // If the number of children changed, then children are different.
  // If there are no children, use shallowCompare in parent function
  // to determine if component should update (false && true == false)

  if (_react2.default.Children.count(children) !== _react2.default.Children.count(nextChildren) || !_react2.default.Children.count(children) || !_react2.default.Children.count(nextChildren)) {
    return true;
  }

  var childrenArray = _react2.default.Children.toArray(children);
  var nextChildrenArray = _react2.default.Children.toArray(nextChildren);

  // React.Children.toArray strip's `false` children so lengths
  // can change
  if (childrenArray.length !== nextChildrenArray.length) {
    return false;
  }

  return [].concat(childrenArray).some(function (child, i) {
    var nextChild = nextChildrenArray[i];

    if (!child.props || !nextChild.props) {
      return !(0, _shallowEqual2.default)(child, nextChild);
    }

    /* eslint-disable no-use-before-define */
    return deepCompareChildren(child, nextChild.props, nextChild.state);
  });
}

function deepCompareChildren(instance, nextProps, nextState) {
  if (!instance.props.children) return (0, _shallowCompare2.default)(instance, nextProps, nextState);

  return (0, _shallowCompare2.default)(instance, nextProps, nextState) || compareChildren(instance.props, nextProps);
}